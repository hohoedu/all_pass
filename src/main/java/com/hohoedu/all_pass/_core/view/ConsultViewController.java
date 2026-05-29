package com.hohoedu.all_pass._core.view;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        List<InflowRoute> routes = consultService.findInflowRoute();

        model.addAttribute("grades", grades);
        model.addAttribute("routes", routes);

        return "/consult/consult";
    }

    @GetMapping("/print-consult")
    public String getPrintTimeView(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) String userCode,
                                   @RequestParam(defaultValue = "all") String typeSort,
                                   @RequestParam(defaultValue = "all") String progressSort,
                                   Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            endDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            startDate = now.minusMonths(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        List<ConsultRespDTO.ConsultPrintDTO> consults = consultService.findConsultForPrint(userCode, startDate, endDate);

        List<String> typeOrder = List.of("hoho", "han", "book");
        List<String> progressOrder = List.of("confirmed", "waiting", "counseling", "ended");

        if (!typeSort.equals("all")) {
            typeOrder = new ArrayList<>(typeOrder);
            typeOrder.remove(typeSort);
            typeOrder.add(0, typeSort);
        }
        if (!progressSort.equals("all")) {
            progressOrder = new ArrayList<>(progressOrder);
            progressOrder.remove(progressSort);
            progressOrder.add(0, progressSort);
        }

        List<String> finalTypeOrder = typeOrder;
        List<String> finalProgressOrder = progressOrder;

        consults.sort(Comparator
                .comparingInt((ConsultRespDTO.ConsultPrintDTO c) ->
                        finalTypeOrder.indexOf(c.getType()) == -1 ? 99 : finalTypeOrder.indexOf(c.getType()))
                .thenComparingInt(c ->
                        finalProgressOrder.indexOf(c.getProgressKey()) == -1 ? 99 : finalProgressOrder.indexOf(c.getProgressKey()))
        );

        String userName = consultService.getUserName(userCode);
        model.addAttribute("consults", consults);
        model.addAttribute("days", DAYS);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("userName", userName);

        return "print/print-consult";
    }

    @GetMapping("/excel-consult")
    public void downloadConsultExcel(@RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam(required = false) String userCode,
                                     HttpSession session,
                                     HttpServletResponse response) throws IOException {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            endDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            startDate = now.minusMonths(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        List<ConsultRespDTO.ConsultPrintDTO> consults = consultService.findConsultForPrint(userCode, startDate, endDate);
        String userName = consultService.getUserName(userCode);

        String fileName = URLEncoder.encode("상담문의기록_" + userName + "_" + startDate + "~" + endDate + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("상담문의기록");

        // ── 스타일 ──────────────────────────────
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

        // ── 헤더 ────────────────────────────────
        String[] headers = {"No", "일자", "이름", "학교", "학년", "연락처", "메모사항", "유입경로", "진행상황", "발송/입회일"};
        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short) 500);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ── 데이터 ──────────────────────────────
        int rowNum = 1;
        for (ConsultRespDTO.ConsultPrintDTO c : consults) {
            Row row = sheet.createRow(rowNum);
            row.setHeight((short) -1);  // ✅ 자동 높이

            createCell(row, 0, String.valueOf(rowNum), centerStyle);
            createCell(row, 1, c.getConsultDate(), centerStyle);
            createCell(row, 2, c.getStudentName(), centerStyle);
            createCell(row, 3, c.getSchool(), centerStyle);
            createCell(row, 4, c.getGradeName(), centerStyle);
            createCell(row, 5, c.getPhone(), centerStyle);
            createCell(row, 6, c.getContent(), memoStyle);
            createCell(row, 7, c.getInflowRouteName(), centerStyle);
            createCell(row, 8, c.getProgressName(), centerStyle);
            createCell(row, 9, c.getSendAt() != null ? c.getSendAt() : "-", centerStyle);

            rowNum++;
        }

        // ── 컬럼 너비 ───────────────────────────
        int[] colWidths = {2000, 4000, 3000, 5000, 3000, 5000, 12000, 4000, 4000, 5000};
        for (int i = 0; i < colWidths.length; i++) {
            sheet.setColumnWidth(i, colWidths[i]);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
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
