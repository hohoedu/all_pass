package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass.payment._dto.web.ReconcileDTO;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * 외부 매출자료(여신금융협회 / 홈택스) ↔ erp_payment_callback 승인번호 대조.
 * 일자는 홈택스·여신 반영 지연이 있어 대조 키에서 제외하고, 승인번호(appr_num)만 대조한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconcileService {

    private final PaymentRepository paymentRepository;

    // 헤더 자동 탐지용 키워드(공백 제거 후 부분 일치). 여신/홈택스 양쪽 포맷 대응.
    private static final List<String> H_APPR = List.of("승인번호");
    private static final List<String> H_GUBUN = List.of("구분", "거래구분");
    private static final List<String> H_DATE = List.of("거래일자", "거래일", "승인일자", "작성일자", "거래일시");
    private static final List<String> H_CARD = List.of("카드사", "발급사", "가맹점", "카드종류");
    private static final List<String> H_AMOUNT = List.of("승인금액", "거래금액", "합계금액", "금액");

    /**
     * 엑셀 업로드 → 대조 결과
     * <p>
     * 승인번호 단위로 엑셀 순금액(승인 - 취소)과 DB 유효 결제 금액(취소 제외)을 비교한다.
     * - 여신 엑셀의 취소 행은 원 승인번호 + 음수 금액으로 들어오고, 이전 달 승인분 취소도 섞여 있다.
     * - 같은 승인번호가 서로 다른 거래에 재사용되기도 해서, 승인번호별로 금액을 합산해 비교한다.
     * - 결제선생 취소는 콜백이 따로 쌓이지 않고 청구서가 canceled 로 바뀌므로 DB 취소는 청구서 상태로 판단한다.
     */
    public ReconcileDTO.Result reconcile(MultipartFile file, String source, String centerCode) throws IOException {
        List<ReconcileDTO.ExcelRow> rows = parseExcel(file);

        // 1) 엑셀 : 승인 / 취소 분리, 승인번호별 순금액
        Map<String, List<ReconcileDTO.ExcelRow>> fileApprovals = new LinkedHashMap<>();
        Map<String, List<ReconcileDTO.ExcelRow>> fileCancels = new LinkedHashMap<>();
        List<ReconcileDTO.ExcelRow> noApprNum = new ArrayList<>();
        int approvedCount = 0, canceledCount = 0;
        long fileApprovedAmount = 0, fileCanceledAmount = 0;
        LocalDate minD = null, maxD = null;

        for (ReconcileDTO.ExcelRow r : rows) {
            boolean isCancel = isCancelGubun(r.getGubun()) || parseAmount(r.getAmount()) < 0;
            long amount = Math.abs(parseAmount(r.getAmount()));
            String n = normalizeApprNum(r.getApprNum());

            if (isCancel) {
                canceledCount++;
                fileCanceledAmount += amount;
            } else {
                approvedCount++;
                fileApprovedAmount += amount;
            }

            if (n == null) {
                if (!isCancel) noApprNum.add(r);
            } else {
                (isCancel ? fileCancels : fileApprovals).computeIfAbsent(n, k -> new ArrayList<>()).add(r);
            }

            String d = normalizeDate(r.getTxDate());
            if (d != null) {
                LocalDate ld = LocalDate.parse(d);
                if (minD == null || ld.isBefore(minD)) minD = ld;
                if (maxD == null || ld.isAfter(maxD)) maxD = ld;
            }
        }
        if (minD == null) {
            throw new IllegalArgumentException("엑셀에서 거래일자를 찾을 수 없습니다. 파일 형식을 확인해주세요.");
        }

        // 2) DB 조회 : 엑셀 첫 거래일 -3일(반영 지연 흡수) ~ 엑셀 마지막 거래일
        //    (월말까지 조회하면 부분월 엑셀에서 이후 결제가 전부 '파일 없음'으로 잡힘)
        String from = minD.minusDays(3).toString();
        String to = maxD.toString();

        List<ReconcileDTO.CallbackRow> dbRows = new ArrayList<>();
        dbRows.addAll(paymentRepository.findCallbacksForReconcile(centerCode, from, to));
        dbRows.addAll(paymentRepository.findManualGroupsForReconcile(centerCode, from, to)); // 수기 카드결제

        Map<String, List<ReconcileDTO.CallbackRow>> dbByNorm = new LinkedHashMap<>();
        for (ReconcileDTO.CallbackRow c : dbRows) {
            String n = normalizeApprNum(c.getApprNum());
            if (n == null) continue;
            if (isCancelState(c.getApprState())) c.setCanceled(true);
            dbByNorm.computeIfAbsent(n, k -> new ArrayList<>()).add(c);
        }

        // 3) 승인번호별 비교
        List<ReconcileDTO.ExcelRow> missingInDb = new ArrayList<>(noApprNum);
        List<ReconcileDTO.CallbackRow> missingInFile = new ArrayList<>();
        List<ReconcileDTO.MismatchRow> cancelNotReflected = new ArrayList<>();
        List<ReconcileDTO.MismatchRow> amountMismatch = new ArrayList<>();
        List<ReconcileDTO.ExcelRow> prevMonthCancels = new ArrayList<>();
        List<ReconcileDTO.CallbackRow> unknownCenter = new ArrayList<>();
        int matched = 0;
        int dbCount = 0; // DB 합계(dbAmount)에 들어간 건수 (센터 미확인·기간 밖·취소 제외)
        long dbAmount = 0, prevCancelAmount = 0, unknownCenterAmount = 0;

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(fileApprovals.keySet());
        keys.addAll(fileCancels.keySet());
        keys.addAll(dbByNorm.keySet());

        String minDate = minD.toString(), maxDate = maxD.toString();

        for (String key : keys) {
            List<ReconcileDTO.ExcelRow> appr = fileApprovals.getOrDefault(key, List.of());
            List<ReconcileDTO.ExcelRow> canc = fileCancels.getOrDefault(key, List.of());
            List<ReconcileDTO.CallbackRow> db = dbByNorm.getOrDefault(key, List.of());

            long fileAppr = appr.stream().mapToLong(r -> Math.abs(parseAmount(r.getAmount()))).sum();
            long fileCanc = canc.stream().mapToLong(r -> Math.abs(parseAmount(r.getAmount()))).sum();
            long fileNet = fileAppr - fileCanc;
            long dbActive = db.stream().filter(c -> !c.isCanceled()).mapToLong(c -> parseAmount(c.getApprPrice())).sum();
            boolean inFile = !appr.isEmpty() || !canc.isEmpty();

            // 이전 달 승인분 취소 : 이번 달 엑셀엔 취소만 있음
            if (appr.isEmpty() && !canc.isEmpty()) {
                prevMonthCancels.addAll(canc);
                prevCancelAmount += fileCanc;
                if (dbActive > 0) {
                    cancelNotReflected.add(mismatch(key, canc.get(0), -fileCanc, dbActive,
                            "이전 달 승인 취소 → DB 결제 유지"));
                    dbAmount += dbActive;
                    dbCount += activeCount(db);
                }
                continue;
            }

            if (!inFile) {
                // DB에만 있음 : 취소건·엑셀 기간 밖 건은 제외, 센터 미확인 건은 별도
                for (ReconcileDTO.CallbackRow c : db) {
                    if (c.isCanceled()) continue;
                    String d = normalizeDate(c.getApprDate());
                    if (d == null || d.compareTo(minDate) < 0 || d.compareTo(maxDate) > 0) continue;
                    long price = parseAmount(c.getApprPrice());
                    if (c.getCenterCode() == null || c.getCenterCode().isBlank()) {
                        unknownCenter.add(c);
                        unknownCenterAmount += price;
                    } else {
                        missingInFile.add(c);
                        dbAmount += price;
                        dbCount++;
                    }
                }
                continue;
            }

            dbAmount += dbActive;
            dbCount += activeCount(db);
            ReconcileDTO.ExcelRow rep = appr.get(0);

            if (db.isEmpty()) {
                // 엑셀에서 승인 후 같은 달에 전액 취소된 거래는 DB에 없어도 정상
                if (fileNet != 0) missingInDb.addAll(appr);
                else matched++;
                continue;
            }

            if (fileNet == dbActive) {
                matched++;
            } else if (fileNet == 0 && !canc.isEmpty()) {
                cancelNotReflected.add(mismatch(key, rep, fileNet, dbActive, "카드사 취소 → DB 결제 유지"));
            } else if (dbActive == 0) {
                amountMismatch.add(mismatch(key, rep, fileNet, dbActive, "DB에서만 취소됨"));
            } else {
                amountMismatch.add(mismatch(key, rep, fileNet, dbActive,
                        canc.isEmpty() ? "금액 다름" : "부분 취소 금액 다름"));
            }
        }

        long fileNetAmount = fileApprovedAmount - fileCanceledAmount;

        return ReconcileDTO.Result.builder()
                .source(source)
                .fileTotal(rows.size())
                .fileApproved(approvedCount)
                .fileCanceled(canceledCount)
                .dbTotal(dbCount)
                .matched(matched)
                .dateFrom(minDate)
                .dateTo(maxDate)
                .fileApprovedAmount(fileApprovedAmount)
                .fileCanceledAmount(fileCanceledAmount)
                .fileNetAmount(fileNetAmount)
                .dbAmount(dbAmount)
                .diffAmount(fileNetAmount - dbAmount)
                .prevCancelAmount(prevCancelAmount)
                .unknownCenterAmount(unknownCenterAmount)
                .missingInDb(missingInDb)
                .missingInFile(missingInFile)
                .cancelNotReflected(cancelNotReflected)
                .amountMismatch(amountMismatch)
                .prevMonthCancels(prevMonthCancels)
                .unknownCenter(unknownCenter)
                .build();
    }

    private ReconcileDTO.MismatchRow mismatch(String apprNum, ReconcileDTO.ExcelRow rep, long fileAmount,
                                              long dbAmount, String note) {
        return ReconcileDTO.MismatchRow.builder()
                .apprNum(apprNum)
                .txDate(rep.getTxDate())
                .cardName(rep.getCardName())
                .fileAmount(fileAmount)
                .dbAmount(dbAmount)
                .note(note)
                .build();
    }

    private int activeCount(List<ReconcileDTO.CallbackRow> db) {
        return (int) db.stream().filter(c -> !c.isCanceled()).count();
    }

    private boolean isCancelGubun(String gubun) {
        return gubun != null && gubun.contains("취소");
    }

    /** 결제선생 appr_state 가 취소를 뜻하는지 (값 체계가 확인되지 않아 보수적으로 판단) */
    private boolean isCancelState(String state) {
        if (state == null) return false;
        String s = state.trim().toLowerCase();
        return s.equals("c") || s.contains("cancel") || s.contains("취소");
    }

    /** "140,000" / "-140,000" / "140000원" → long (숫자 없으면 0) */
    static long parseAmount(String raw) {
        if (raw == null) return 0;
        String t = raw.trim();
        boolean negative = t.startsWith("-") || (t.startsWith("(") && t.endsWith(")"));
        int dot = t.indexOf('.');
        if (dot >= 0) t = t.substring(0, dot); // "140000.00" 같은 소수점 이하 제거
        String d = t.replaceAll("\\D", "");
        if (d.isEmpty()) return 0;
        long v = Long.parseLong(d);
        return negative ? -v : v;
    }

    /** 엑셀 파싱: 헤더 행을 자동 탐지(승인번호 컬럼 기준)하여 필요한 컬럼만 추출 */
    private List<ReconcileDTO.ExcelRow> parseExcel(MultipartFile file) throws IOException {
        List<ReconcileDTO.ExcelRow> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            // 1) 헤더 행 탐지: '승인번호' 셀이 있는 행
            int headerRow = -1;
            Map<String, Integer> col = null;
            for (int r = sheet.getFirstRowNum(); r <= Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + 30); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, Integer> found = resolveColumns(row, fmt);
                if (found.containsKey("appr")) {
                    headerRow = r;
                    col = found;
                    break;
                }
            }
            if (headerRow < 0 || col == null) {
                throw new IllegalArgumentException("엑셀에서 '승인번호' 열을 찾을 수 없습니다. 파일 형식을 확인해주세요.");
            }

            // 2) 데이터 행 파싱
            int cAppr = col.get("appr");
            Integer cGubun = col.get("gubun");
            Integer cDate = col.get("date");
            Integer cCard = col.get("card");
            Integer cAmount = col.get("amount");

            for (int r = headerRow + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String appr = cellStr(row, cAppr, fmt);
                if (appr == null || appr.isBlank()) continue; // 빈 줄/푸터 skip

                rows.add(ReconcileDTO.ExcelRow.builder()
                        .apprNum(appr)
                        .gubun(cGubun == null ? "" : cellStr(row, cGubun, fmt))
                        .txDate(cDate == null ? "" : cellStr(row, cDate, fmt))
                        .cardName(cCard == null ? "" : cellStr(row, cCard, fmt))
                        .amount(cAmount == null ? "" : cellStr(row, cAmount, fmt))
                        .build());
            }
        }
        return rows;
    }

    /** 한 행에서 헤더 키워드로 컬럼 인덱스 매핑 */
    private Map<String, Integer> resolveColumns(Row row, DataFormatter fmt) {
        Map<String, Integer> col = new HashMap<>();
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            String v = cellStr(row, c, fmt);
            if (v == null) continue;
            String t = v.replaceAll("\\s", "");
            if (t.isEmpty()) continue;
            putIfMatch(col, "appr", t, H_APPR, c);
            putIfMatch(col, "gubun", t, H_GUBUN, c);
            putIfMatch(col, "date", t, H_DATE, c);
            putIfMatch(col, "card", t, H_CARD, c);
            putIfMatch(col, "amount", t, H_AMOUNT, c);
        }
        return col;
    }

    private void putIfMatch(Map<String, Integer> col, String key, String text, List<String> keywords, int idx) {
        if (col.containsKey(key)) return;
        for (String kw : keywords) {
            if (text.contains(kw)) { col.put(key, idx); return; }
        }
    }

    private String cellStr(Row row, int c, DataFormatter fmt) {
        if (c < 0) return null;
        Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return fmt.formatCellValue(cell).trim();
    }

    /** 승인번호 정규화: 숫자만 남기고, 8자리 이하이면 앞자리 0 보정 */
    static String normalizeApprNum(String raw) {
        if (raw == null) return null;
        String d = raw.replaceAll("\\D", "");
        if (d.isEmpty()) return null;
        if (d.length() < 8) d = String.format("%8s", d).replace(' ', '0');
        return d;
    }

    /** 거래일자 → yyyy-MM-dd (앞 8자리 숫자 사용) */
    static String normalizeDate(String raw) {
        if (raw == null) return null;
        String d = raw.replaceAll("\\D", "");
        if (d.length() < 8) return null;
        d = d.substring(0, 8);
        return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8);
    }

    /** 대조 결과 → 엑셀(요약 + 불일치 목록) */
    public byte[] exportResult(ReconcileDTO.Result result) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle header = headerStyle(wb);

            // 시트0: 요약
            Sheet s0 = wb.createSheet("요약");
            String[][] summary = {
                    {"대조 기간", nz(result.getDateFrom()) + " ~ " + nz(result.getDateTo())},
                    {"엑셀 승인 합계", String.format("%,d", result.getFileApprovedAmount())},
                    {"엑셀 취소 합계", String.format("%,d", result.getFileCanceledAmount())},
                    {"엑셀 순매출", String.format("%,d", result.getFileNetAmount())},
                    {"DB 카드 결제 합계", String.format("%,d", result.getDbAmount())},
                    {"차이 (엑셀 - DB)", String.format("%,d", result.getDiffAmount())},
                    {"└ 이전 달 승인 취소분", String.format("%,d", result.getPrevCancelAmount())},
                    {"센터 미확인 DB 결제 (제외)", String.format("%,d", result.getUnknownCenterAmount())},
            };
            for (int i = 0; i < summary.length; i++) {
                Row row = s0.createRow(i);
                Cell k = row.createCell(0);
                k.setCellValue(summary[i][0]);
                k.setCellStyle(header);
                row.createCell(1).setCellValue(summary[i][1]);
            }
            autoSize(s0, 2);

            // 시트1: 매출 확인 필요(파일엔 있고 DB엔 없음)
            Sheet s1 = wb.createSheet("매출확인필요");
            String[] h1 = {"구분", "거래일자", "카드사", "승인번호", "승인금액"};
            writeHeader(s1, h1, header);
            int ri = 1;
            if (result.getMissingInDb() != null) {
                for (ReconcileDTO.ExcelRow r : result.getMissingInDb()) {
                    Row row = s1.createRow(ri++);
                    row.createCell(0).setCellValue(nz(r.getGubun()));
                    row.createCell(1).setCellValue(nz(r.getTxDate()));
                    row.createCell(2).setCellValue(nz(r.getCardName()));
                    row.createCell(3).setCellValue(nz(r.getApprNum()));
                    row.createCell(4).setCellValue(nz(r.getAmount()));
                }
            }
            autoSize(s1, h1.length);

            // 시트2: 반영지연·취소 의심(DB엔 있고 파일엔 없음)
            Sheet s2 = wb.createSheet("반영지연_취소의심");
            String[] h2 = {"승인일시", "카드/은행", "승인번호", "승인금액", "상태"};
            writeHeader(s2, h2, header);
            ri = 1;
            if (result.getMissingInFile() != null) {
                for (ReconcileDTO.CallbackRow c : result.getMissingInFile()) {
                    Row row = s2.createRow(ri++);
                    row.createCell(0).setCellValue(nz(c.getApprDate()));
                    row.createCell(1).setCellValue(nz(c.getApprIssuer()));
                    row.createCell(2).setCellValue(nz(c.getApprNum()));
                    row.createCell(3).setCellValue(nz(c.getApprPrice()));
                    row.createCell(4).setCellValue(nz(c.getApprState()));
                }
            }
            autoSize(s2, h2.length);

            // 시트3: 취소 미반영 + 금액 불일치
            Sheet s3 = wb.createSheet("취소미반영_금액불일치");
            String[] h3 = {"구분", "거래일자", "카드사", "승인번호", "엑셀 순금액", "DB 금액", "내용"};
            writeHeader(s3, h3, header);
            ri = 1;
            ri = writeMismatch(s3, ri, "취소 미반영", result.getCancelNotReflected());
            writeMismatch(s3, ri, "금액 불일치", result.getAmountMismatch());
            autoSize(s3, h3.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private int writeMismatch(Sheet sheet, int ri, String type, List<ReconcileDTO.MismatchRow> list) {
        if (list == null) return ri;
        for (ReconcileDTO.MismatchRow m : list) {
            Row row = sheet.createRow(ri++);
            row.createCell(0).setCellValue(type);
            row.createCell(1).setCellValue(nz(m.getTxDate()));
            row.createCell(2).setCellValue(nz(m.getCardName()));
            row.createCell(3).setCellValue(nz(m.getApprNum()));
            row.createCell(4).setCellValue(m.getFileAmount());
            row.createCell(5).setCellValue(m.getDbAmount());
            row.createCell(6).setCellValue(nz(m.getNote()));
        }
        return ri;
    }

    private void writeHeader(Sheet sheet, String[] headers, CellStyle style) {
        Row hdr = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hdr.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
