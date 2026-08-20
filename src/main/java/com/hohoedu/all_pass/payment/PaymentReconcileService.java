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

    /** 엑셀 업로드 → 대조 결과 */
    public ReconcileDTO.Result reconcile(MultipartFile file, String source, String centerCode) throws IOException {
        List<ReconcileDTO.ExcelRow> rows = parseExcel(file);

        // 승인 건만 대조 대상(취소 제외)
        List<ReconcileDTO.ExcelRow> approved = new ArrayList<>();
        for (ReconcileDTO.ExcelRow r : rows) {
            String g = r.getGubun() == null ? "" : r.getGubun().trim();
            if (g.isEmpty() || g.contains("승인")) {
                approved.add(r);
            }
        }

        // 엑셀 거래일자 최소/최대 산출
        LocalDate minD = null, maxD = null;
        for (ReconcileDTO.ExcelRow r : approved) {
            String d = normalizeDate(r.getTxDate());
            if (d == null) continue;
            LocalDate ld = LocalDate.parse(d);
            if (minD == null || ld.isBefore(minD)) minD = ld;
            if (maxD == null || ld.isAfter(maxD)) maxD = ld;
        }
        if (minD == null) {
            throw new IllegalArgumentException("엑셀에서 거래일자를 찾을 수 없습니다. 파일 형식을 확인해주세요.");
        }
        // 대조 범위: 거래월 1일 -3일 ~ 거래월 말일 (월초 반영지연 흡수)
        String from = minD.withDayOfMonth(1).minusDays(3).toString();
        String to = java.time.YearMonth.from(maxD).atEndOfMonth().toString();

        // DB 콜백 조회(거래일 범위) → 승인번호 정규화 맵
        List<ReconcileDTO.CallbackRow> callbacks =
                paymentRepository.findCallbacksForReconcile(centerCode, from, to);
        // 수기결제(오프라인 카드) 승인번호도 대조 대상에 포함
        List<ReconcileDTO.CallbackRow> manualGroups =
                paymentRepository.findManualGroupsForReconcile(centerCode, from, to);

        Map<String, ReconcileDTO.CallbackRow> dbByNorm = new LinkedHashMap<>();
        for (ReconcileDTO.CallbackRow c : callbacks) {
            String n = normalizeApprNum(c.getApprNum());
            if (n == null) continue;
            dbByNorm.putIfAbsent(n, c); // 승인/취소 중복 시 첫 건 대표
        }
        for (ReconcileDTO.CallbackRow c : manualGroups) {
            String n = normalizeApprNum(c.getApprNum());
            if (n == null) continue;
            dbByNorm.putIfAbsent(n, c);
        }

        // 파일 승인번호 정규화 집합
        Set<String> fileNorms = new HashSet<>();
        for (ReconcileDTO.ExcelRow r : approved) {
            String n = normalizeApprNum(r.getApprNum());
            if (n != null) fileNorms.add(n);
        }

        // 🔴 파일엔 있으나 DB에 없음
        List<ReconcileDTO.ExcelRow> missingInDb = new ArrayList<>();
        int matched = 0;
        Set<String> counted = new HashSet<>();
        for (ReconcileDTO.ExcelRow r : approved) {
            String n = normalizeApprNum(r.getApprNum());
            if (n == null) { missingInDb.add(r); continue; }
            if (dbByNorm.containsKey(n)) {
                if (counted.add(n)) matched++;
            } else {
                missingInDb.add(r);
            }
        }

        // 🟡 DB엔 있으나 파일에 없음
        List<ReconcileDTO.CallbackRow> missingInFile = new ArrayList<>();
        for (Map.Entry<String, ReconcileDTO.CallbackRow> e : dbByNorm.entrySet()) {
            if (!fileNorms.contains(e.getKey())) {
                missingInFile.add(e.getValue());
            }
        }

        return ReconcileDTO.Result.builder()
                .source(source)
                .fileTotal(rows.size())
                .fileApproved(approved.size())
                .dbTotal(dbByNorm.size())
                .matched(matched)
                .dateFrom(from)
                .dateTo(to)
                .missingInDb(missingInDb)
                .missingInFile(missingInFile)
                .build();
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

    /** 대조 결과 → 엑셀(불일치 목록) */
    public byte[] exportResult(ReconcileDTO.Result result) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle header = headerStyle(wb);

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

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
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
