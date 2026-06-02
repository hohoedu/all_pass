package com.hohoedu.all_pass._core.view;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hohoedu.all_pass.consult.ConsultService;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import com.hohoedu.all_pass.consult.model.InflowRoute;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.GradeCode;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import static com.hohoedu.all_pass._core.vo.Constants.DAYS;

@Controller
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultViewController {

    private final StudentService studentService;
    private final ConsultService consultService;
    private final UserService userService;

    @GetMapping("consult_v2")
    public String getConsultV2Page(Model model, HttpServletRequest request, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirectUrl=" + request.getRequestURI();
        }

        return "consult";
    }

    @GetMapping("/main")
    public String getConsultMainPage(Model model, HttpServletRequest request, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirectUrl=" + request.getRequestURI();
        }

        List<GradeCode> grades = studentService.findGrade();
        List<InflowRoute> inflowRoutes = consultService.findInflowRoute();

        model.addAttribute("grades", grades);
        model.addAttribute("inflowRoutes", inflowRoutes);

        return "/consult/consult";
    }

    @GetMapping("/print-consult")
    public String printConsult(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "all") String progress,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "consultDate") String sortColumn,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        ConsultReqDTO.ConsultPrintReqDTO reqDTO = buildPrintReqDTO(user, startDate, endDate, progress, keyword, sortColumn, sortDir);
        List<ConsultRespDTO.ConsultPrintDTO> consults = consultService.findConsultForPrint(reqDTO);

        model.addAttribute("consults", consults);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("userName", user.getUserName());

        return "print/print-consult";
    }

    @GetMapping("/excel-consult")
    public void downloadConsultExcel(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "all") String progress,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "consultDate") String sortColumn,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session, HttpServletResponse response) throws IOException {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) { response.sendRedirect("/login"); return; }

        ConsultReqDTO.ConsultPrintReqDTO reqDTO = buildPrintReqDTO(user, startDate, endDate, progress, keyword, sortColumn, sortDir);
        List<ConsultRespDTO.ConsultPrintDTO> consults = consultService.findConsultForPrint(reqDTO);

        String fileName = URLEncoder.encode(
                "상담문의기록_" + startDate + "~" + endDate + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // 기존 엑셀 생성 코드 그대로 사용
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("상담문의기록");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(headerStyle);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centerStyle.setWrapText(true);
        setBorder(centerStyle);

        CellStyle memoStyle = workbook.createCellStyle();
        memoStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        memoStyle.setWrapText(true);
        setBorder(memoStyle);

        String[] headers = {"No", "일자", "이름", "학교", "학년", "연락처", "메모사항", "유입경로", "진행상황", "발송/입회일"};
        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short) 500);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ConsultRespDTO.ConsultPrintDTO c : consults) {
            Row row = sheet.createRow(rowNum);
            row.setHeight((short) -1);
            createCell(row, 0, String.valueOf(rowNum), centerStyle);
            createCell(row, 1, c.getConsultDate(),     centerStyle);
            createCell(row, 2, c.getStudentName(),     centerStyle);
            createCell(row, 3, c.getSchool(),          centerStyle);
            createCell(row, 4, c.getGradeName(),       centerStyle);
            createCell(row, 5, c.getPhone(),           centerStyle);
            createCell(row, 6, c.getContent(),         memoStyle);
            createCell(row, 7, c.getInflowRouteName(), centerStyle);
            createCell(row, 8, c.getProgressName(),    centerStyle);
            createCell(row, 9, c.getSendAt() != null ? c.getSendAt() : "-", centerStyle);
            rowNum++;
        }

        int[] colWidths = {2000, 4000, 3000, 5000, 3000, 5000, 12000, 4000, 4000, 5000};
        for (int i = 0; i < colWidths.length; i++) sheet.setColumnWidth(i, colWidths[i]);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private ConsultReqDTO.ConsultPrintReqDTO buildPrintReqDTO(
            UserRespDTO.LoginRespDTO user,
            String startDate, String endDate,
            String progress, String keyword,
            String sortColumn, String sortDir) {

        ConsultReqDTO.ConsultPrintReqDTO req = new ConsultReqDTO.ConsultPrintReqDTO();
        req.setCenterCode(user.getCenterCode());
        req.setStartDate(startDate);
        req.setEndDate(endDate);
        req.setProgress(progress.equals("all") ? null : progress);
        req.setKeyword(keyword);
        req.setSortColumn(sortColumn);
        req.setSortDir(sortDir);
        return req;
    }

    // ── 헬퍼 ────────────────────────────────────
    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    @GetMapping("/test")
    public String getConsultTestPage(Model model) {

        return "consult/consult-test";
    }

    @GetMapping("/level")
    public String getConsultLevelPage(Model model) {

        return "consult/level-test_h";
    }

}
