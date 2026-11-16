package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminServiceMemberChangeTest {

    private static AdminRespDTO.MemberChangeRowDTO row(int mm, String subject, String teacher, String kind, double val) {
        AdminRespDTO.MemberChangeRowDTO r = new AdminRespDTO.MemberChangeRowDTO();
        r.setMm(mm);
        r.setSubject(subject);
        r.setTeacherName(teacher);
        r.setKind(kind);
        r.setVal(val);
        return r;
    }

    @Test
    void aggregatesAndWritesExcel() throws Exception {
        List<AdminRespDTO.MemberChangeRowDTO> rows = List.of(
                row(1, "book", "김선생", "member", 2.5),
                row(1, "book", "남지영", "member", 1),
                row(1, "book", "조정로", "member", 5),
                row(1, "book", "이경진", "member", 0), // 빈 시간표 → 칸 안 생김
                row(1, "book", "이선생", "member", 1.25),
                row(1, "book", "김선생", "join", 1),
                row(1, "book", "김선생", "join", 1),
                row(1, "book", null, "withdraw", 1),
                row(1, "book", null, "consult", 1),
                row(3, "hoho", "박선생", "member", 0.75),
                row(2, "han", "가", "join", 1), row(2, "han", "나", "join", 1),
                row(2, "han", "다", "join", 1), row(2, "han", "라", "join", 1));
        AdminService service = new AdminService(null, null, null, null, null);

        List<AdminRespDTO.MemberChangeSubject> report = AdminService.aggregateMemberChange(rows);
        AdminRespDTO.MemberChangeSubject book = report.get(0);
        assertEquals(List.of("남지영", "김선생", "이선생"), book.teachers()); // 지정 순서 먼저, 미지정·조정로 제외
        AdminRespDTO.MemberChangeMonth jan = book.months().get(0);
        assertEquals(4.75, jan.total());
        assertEquals(1, jan.consult());
        assertEquals(2, jan.cells().get("김선생").get("join"));
        assertEquals(0.75, report.get(2).months().get(2).total());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeMemberChangeExcel(report, java.util.Map.of(3, "신학기"), "테스트센터", "2026", out);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            assertEquals(List.of("전체 현황", "한스쿨 현황", "북스쿨 현황", "호호스쿨 현황"),
                    List.of(wb.getSheetName(0), wb.getSheetName(1), wb.getSheetName(2), wb.getSheetName(3)));
            var sum = wb.getSheet("전체 현황");
            var bookSh = wb.getSheet("북스쿨 현황");
            var han = wb.getSheet("한스쿨 현황");
            var hohoSh = wb.getSheet("호호스쿨 현황");
            assertEquals("26년 테스트센터 회원 변동", sum.getRow(0).getCell(0).getStringCellValue());
            assertEquals("SUM(J5,P5,V5)", bookSh.getRow(4).getCell(3).getCellFormula()); // 북스쿨 1월 전체총원
            assertEquals(4.75, bookSh.getRow(4).getCell(3).getNumericCellValue());
            assertEquals(4.75, sum.getRow(4).getCell(3).getNumericCellValue());         // 전체 1월 전체총원
            assertEquals(2, sum.getRow(4).getCell(4).getNumericCellValue());            // 전체 1월 북 입회
            assertEquals(4, sum.getRow(5).getCell(5).getNumericCellValue());            // 전체 2월 한 입회 (4명)
            assertEquals("'호호스쿨 현황'!D7", sum.getRow(6).getCell(16).getCellFormula()); // 전체 3월 호호 회원 = 호호 전체총원
            assertEquals(0.75, sum.getRow(6).getCell(16).getNumericCellValue());
            assertEquals(0.75, hohoSh.getRow(6).getCell(3).getNumericCellValue());
            assertEquals("라", han.getRow(2).getCell(22).getStringCellValue());       // 한스쿨 4번째 선생님 W3
            assertEquals("입회", han.getRow(3).getCell(22).getStringCellValue());
            assertEquals("신학기", sum.getRow(6).getCell(0).getStringCellValue());     // 행사 or 이슈 3월 A7
            assertEquals("신학기", bookSh.getRow(6).getCell(0).getStringCellValue());
            // 블록을 맨 위로 올린 뒤에도 병합(제목 A1:V1, 헤더 A3:A4, 선생님 E3:J3)이 맞는 위치에 있는지
            var merges = hohoSh.getMergedRegions().stream().map(m -> m.formatAsString()).toList();
            assertEquals(true, merges.containsAll(List.of("A1:V1", "A3:A4", "E3:J3")), merges.toString());
            assertEquals(true, sum.getMergedRegions().stream().allMatch(m -> m.getLastRow() <= 18));
            assertEquals(true, sum.getMergedRegions().stream().anyMatch(m -> m.formatAsString().equals("A1:R1")));
            for (int i = 0; i < 4; i++) { // 인쇄 1페이지 맞춤
                var ps = wb.getSheetAt(i).getPrintSetup();
                assertEquals(true, wb.getSheetAt(i).getFitToPage());
                assertEquals(1, ps.getFitWidth());
                assertEquals(1, ps.getFitHeight());
            }
        }
    }
}
