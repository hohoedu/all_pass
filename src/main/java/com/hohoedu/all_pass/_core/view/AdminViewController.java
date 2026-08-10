package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass.admin.AdminService;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.CenterService;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.logistics.LogisticsService;
import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hohoedu.all_pass._core.vo.Constants.DAYS;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor()
public class AdminViewController {

    private final ClassService classService;
    private final CenterService centerService;
    private final AdminService adminService;
    private final LogisticsService logisticsService;
    private final UserService userService;

    @GetMapping("/order/order-list2")
    public String getAdminOrderList(Model model, HttpSession session) {
        List<Center> center = centerService.findAllCenter();
        adminService.findAdminOrderList();

        Map<String, Integer> deadlineMap = logisticsService.findAllDeadlines()
                .stream()
                .collect(Collectors.toMap(
                        LogisRespDTO.DeadlineDTO::getCenterCode,
                        LogisRespDTO.DeadlineDTO::getDeadlineAt));

        model.addAttribute("center", center);
        model.addAttribute("deadlineMap", deadlineMap);

        return "/admin/order/order-list-regacy";
    }

    @GetMapping("/order/order-list")
    public String getAdminOrderList2(Model model) {
        Map<String, Integer> deadlineMap = logisticsService.findAllDeadlines()
                .stream()
                .collect(Collectors.toMap(
                        LogisRespDTO.DeadlineDTO::getCenterCode,
                        LogisRespDTO.DeadlineDTO::getDeadlineAt));
        Map<String, String> dateConfig = DateConfig.currentYearMonth();
        String year = dateConfig.get("currentYear");
        String month = dateConfig.get("currentMonth");

        log.info("Year: {}, Month: {}", year, month);

        Map<String, LogisRespDTO.ReorderStatusDTO> reorderStatusMap = logisticsService.findReorderStatusMap(year,
                month);
        model.addAttribute("center", centerService.findAllCenter());
        model.addAttribute("deadlineMap", deadlineMap);
        model.addAttribute("reorderStatusMap", reorderStatusMap);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        return "/admin/order/order-list";
    }

    @GetMapping("/order/print-invoice")
    public String printInvoice(@RequestParam(value = "year") String year, @RequestParam(value = "month") String month,
            @RequestParam(value = "centerCode") String centerCode, Model model) {

        LogisRespDTO.SelectCenterDTO.CenterInfoDTO centerInfo = logisticsService.findCenterInfo(centerCode);

        LogisReqDTO.ReorderListReqDTO req = new LogisReqDTO.ReorderListReqDTO();
        req.setYear(year);
        req.setMonth(month);
        req.setCenterCode(centerCode);
        List<LogisRespDTO.InvoiceDTO> items = logisticsService.findInvoice(req);

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("centerInfo", centerInfo);
        model.addAttribute("items", items);
        return "/admin/print/print-invoice";
    }

    @GetMapping("/order/print-manual-invoice")
    public String printManualInvoice(@RequestParam(value = "manualId") String manualId, Model model) {

        LogisReqDTO.ManualItemsReqDTO req = new LogisReqDTO.ManualItemsReqDTO();
        req.setManualId(manualId);

        Map<String, Object> manualInfo = logisticsService.getManualHeader(req);
        List<Map<String, Object>> rawItems = logisticsService.getManualItems(req);

        String centerCode = (String) manualInfo.get("centerCode");
        LogisRespDTO.SelectCenterDTO.CenterInfoDTO centerInfo = logisticsService.findCenterInfo(centerCode);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> item : rawItems) {
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("orderDate", item.get("orderDate"));
            mapped.put("className", item.get("classKey"));
            mapped.put("unitName", item.get("unitKey"));
            mapped.put("totalCount", parseIntSafe(item.get("qty")));
            mapped.put("unitPrice", parseIntSafe(item.get("price")));
            mapped.put("totalPrice", parseIntSafe(item.get("amount")));
            mapped.put("userName", item.get("note"));
            items.add(mapped);
        }

        model.addAttribute("year", manualInfo.get("yy"));
        model.addAttribute("month", manualInfo.get("mm"));
        model.addAttribute("centerInfo", centerInfo);
        model.addAttribute("items", items);

        return "/admin/print/print-invoice";
    }

    private int parseIntSafe(Object val) {
        if (val == null)
            return 0;
        try {
            return Integer.parseInt(String.valueOf(val).replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @GetMapping("/order/invoice-excel")
    public void downloadInvoiceExcel(@RequestParam(value = "year") String year, @RequestParam(value = "month") String month,
            @RequestParam(value = "centerCode") String centerCode, HttpServletResponse response) throws IOException {

        LogisRespDTO.SelectCenterDTO.CenterInfoDTO centerInfo = logisticsService.findCenterInfo(centerCode);

        LogisReqDTO.ReorderListReqDTO req = new LogisReqDTO.ReorderListReqDTO();
        req.setYear(year);
        req.setMonth(month);
        req.setCenterCode(centerCode);
        List<LogisRespDTO.InvoiceDTO> rawItems = logisticsService.findInvoice(req);

        List<Map<String, Object>> items = new ArrayList<>();
        for (LogisRespDTO.InvoiceDTO item : rawItems) {
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("orderDate", item.getOrderDate());
            mapped.put("className", item.getClassName());
            mapped.put("unitName", item.getUnitName());
            mapped.put("totalCount", item.getTotalCount());
            mapped.put("unitPrice", item.getUnitPrice());
            mapped.put("totalPrice", item.getTotalPrice());
            mapped.put("userName", item.getUserName());
            items.add(mapped);
        }

        String title = "거래명세서 (" + year + "." + month + ")";
        String fileNamePrefix = (centerInfo != null ? centerInfo.getCenterName() : "거래명세서") + "_" + year + month;

        generateInvoiceExcel(title, fileNamePrefix, centerInfo, items, response);
    }

    private void generateInvoiceExcel(String title, String fileNamePrefix,
            LogisRespDTO.SelectCenterDTO.CenterInfoDTO centerInfo,
            List<Map<String, Object>> items,
            HttpServletResponse response) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("거래명세서");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle sumStyle = workbook.createCellStyle();
            sumStyle.cloneStyleFrom(cellStyle);
            Font sumFont = workbook.createFont();
            sumFont.setBold(true);
            sumStyle.setFont(sumFont);
            sumStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            sumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            rowIdx++;

            Row infoRow1 = sheet.createRow(rowIdx++);
            infoRow1.createCell(0).setCellValue("상호");
            infoRow1.createCell(1).setCellValue(centerInfo != null ? centerInfo.getCenterName() : "-");
            infoRow1.createCell(2).setCellValue("등록번호");
            infoRow1.createCell(3).setCellValue(centerInfo != null ? centerInfo.getBizNum() : "-");

            Row infoRow2 = sheet.createRow(rowIdx++);
            infoRow2.createCell(0).setCellValue("성명");
            infoRow2.createCell(1).setCellValue(centerInfo != null ? centerInfo.getDirectorName() : "-");
            infoRow2.createCell(2).setCellValue("연락처");
            infoRow2.createCell(3).setCellValue(centerInfo != null ? centerInfo.getManagerTel() : "-");

            Row infoRow3 = sheet.createRow(rowIdx++);
            infoRow3.createCell(0).setCellValue("주소");
            infoRow3.createCell(1).setCellValue(centerInfo != null ? centerInfo.getAddress() : "-");

            rowIdx++;

            String[] headers = { "주문일시", "품명", "규격", "수량", "단가", "금액", "비고" };
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int totalQty = 0;
            long totalAmount = 0;
            String prevDate = null;

            for (Map<String, Object> item : items) {
                Row row = sheet.createRow(rowIdx++);

                String orderDate = String.valueOf(item.get("orderDate"));
                String dateOnly = (orderDate != null && orderDate.length() >= 10) ? orderDate.substring(0, 10) : "";
                String showDate = dateOnly.equals(prevDate) ? "" : dateOnly;
                prevDate = dateOnly;

                int qty = parseIntSafe(item.get("totalCount"));
                int price = parseIntSafe(item.get("unitPrice"));
                int amount = parseIntSafe(item.get("totalPrice"));

                row.createCell(0).setCellValue(showDate);
                row.createCell(1).setCellValue(String.valueOf(item.get("className")));
                row.createCell(2).setCellValue(String.valueOf(item.get("unitName")));
                row.createCell(3).setCellValue(qty);
                row.createCell(4).setCellValue(price);
                row.createCell(5).setCellValue(amount);
                row.createCell(6).setCellValue(String.valueOf(item.get("userName")));

                for (int i = 0; i < 7; i++) {
                    row.getCell(i).setCellStyle(cellStyle);
                }

                totalQty += qty;
                totalAmount += amount;
            }

            Row sumRow = sheet.createRow(rowIdx);
            sumRow.createCell(0).setCellValue("합계");
            sumRow.createCell(1);
            sumRow.createCell(2);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 2));
            sumRow.createCell(3).setCellValue(totalQty);
            sumRow.createCell(4).setCellValue("");
            sumRow.createCell(5).setCellValue(totalAmount);
            sumRow.createCell(6).setCellValue("");
            for (int i = 0; i < 7; i++) {
                sumRow.getCell(i).setCellStyle(sumStyle);
            }

            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 3000);
            sheet.setColumnWidth(3, 2500);
            sheet.setColumnWidth(4, 3500);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 3500);

            String fileName = URLEncoder.encode(fileNamePrefix + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/order/manual-excel")
    public void downloadManualExcel(@RequestParam(value = "manualId") String manualId, HttpServletResponse response) throws IOException {

        LogisReqDTO.ManualItemsReqDTO req = new LogisReqDTO.ManualItemsReqDTO();
        req.setManualId(manualId);

        Map<String, Object> manualInfo = logisticsService.getManualHeader(req);
        List<Map<String, Object>> rawItems = logisticsService.getManualItems(req);

        String centerCode = (String) manualInfo.get("centerCode");
        LogisRespDTO.SelectCenterDTO.CenterInfoDTO centerInfo = logisticsService.findCenterInfo(centerCode);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> item : rawItems) {
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("orderDate", item.get("orderDate"));
            mapped.put("className", item.get("classKey"));
            mapped.put("unitName", item.get("unitKey"));
            mapped.put("totalCount", parseIntSafe(item.get("qty")));
            mapped.put("unitPrice", parseIntSafe(item.get("price")));
            mapped.put("totalPrice", parseIntSafe(item.get("amount")));
            mapped.put("userName", item.get("note"));
            items.add(mapped);
        }

        String yy = String.valueOf(manualInfo.get("yy"));
        String mm = String.valueOf(manualInfo.get("mm"));
        String title = manualInfo.get("title") + " (" + yy + "." + mm + ")";
        String fileNamePrefix = (centerInfo != null ? centerInfo.getCenterName() : "거래명세서") + "_" + yy + mm + "_수기";

        generateInvoiceExcel(title, fileNamePrefix, centerInfo, items, response);
    }

    @GetMapping("/order/print-aggregate")
    public String printAggregate(@RequestParam(value = "year") String year, @RequestParam(value = "month") String month,
            @RequestParam(value = "segmentType") String segmentType, @RequestParam(value = "centerCodes") String centerCodes, Model model) {

        LogisReqDTO.AggregateReqDTO req = new LogisReqDTO.AggregateReqDTO();
        req.setYear(year);
        req.setMonth(month);
        req.setSegmentType(segmentType);
        req.setCenterCodes(Arrays.asList(centerCodes.split(",")));

        List<LogisRespDTO.CenterAggregateDTO> list = "센터별 선생님 집계".equals(segmentType)
                ? logisticsService.findTeacherAggregate(req)
                : logisticsService.findCenterAggregate(req);

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("segmentType", segmentType);
        model.addAttribute("list", list);
        return "/admin/print/print-aggregate";
    }

    @GetMapping("/order/aggregate-excel")
    public void downloadAggregateExcel(@RequestParam(value = "year") String year, @RequestParam(value = "month") String month,
            @RequestParam(value = "segmentType") String segmentType, @RequestParam(value = "centerCodes") String centerCodes, HttpServletResponse response)
            throws IOException {

        LogisReqDTO.AggregateReqDTO req = new LogisReqDTO.AggregateReqDTO();
        req.setYear(year);
        req.setMonth(month);
        req.setSegmentType(segmentType);
        req.setCenterCodes(Arrays.asList(centerCodes.split(",")));

        boolean isTeacherMode = "센터별 선생님 집계".equals(segmentType);
        boolean isAllMode = "전체 집계".equals(segmentType);

        List<LogisRespDTO.CenterAggregateDTO> list = isTeacherMode
                ? logisticsService.findTeacherAggregate(req)
                : logisticsService.findCenterAggregate(req);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(segmentType);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerStyle);

            CellStyle cellStyle = workbook.createCellStyle();
            setBorder(cellStyle);

            CellStyle centerStyle = workbook.createCellStyle();
            setBorder(centerStyle);
            Font centerFont = workbook.createFont();
            centerFont.setBold(true);
            centerStyle.setFont(centerFont);
            centerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            centerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle teacherStyle = workbook.createCellStyle();
            setBorder(teacherStyle);
            Font teacherFont = workbook.createFont();
            teacherFont.setBold(true);
            teacherFont.setColor(IndexedColors.VIOLET.getIndex());
            teacherStyle.setFont(teacherFont);
            teacherStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            teacherStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle subTotalStyle = workbook.createCellStyle();
            subTotalStyle.cloneStyleFrom(teacherStyle);

            CellStyle totalStyle = workbook.createCellStyle();
            setBorder(totalStyle);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int colCount = isTeacherMode ? 8 : isAllMode ? 6 : 7;
            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(segmentType + " (" + year + "." + month + ")");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCount - 1));

            rowIdx++;

            String[] headers = isTeacherMode
                    ? new String[] { "선생님", "단계", "교재", "학생 수량", "선생님 수량", "추가수량", "합계", "시간표 수량" }
                    : isAllMode
                            ? new String[] { "단계", "교재", "학생 수량", "선생님 수량", "추가수량", "합계" }
                            : new String[] { "단계", "교재", "학생 수량", "선생님 수량", "추가수량", "합계", "시간표 수량" };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (LogisRespDTO.CenterAggregateDTO center : list) {
                Row centerRow = sheet.createRow(rowIdx++);
                centerRow.createCell(0).setCellValue(center.getCenterName());
                for (int i = 1; i < colCount; i++)
                    centerRow.createCell(i);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, colCount - 1));
                for (int i = 0; i < colCount; i++)
                    centerRow.getCell(i).setCellStyle(centerStyle);

                if (center.getItems() == null || center.getItems().isEmpty()) {
                    Row emptyRow = sheet.createRow(rowIdx++);
                    emptyRow.createCell(0).setCellValue("데이터가 없습니다.");
                    for (int i = 1; i < colCount; i++)
                        emptyRow.createCell(i);
                    sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, colCount - 1));
                    for (int i = 0; i < colCount; i++)
                        emptyRow.getCell(i).setCellStyle(cellStyle);
                    continue;
                }

                int totalBaseCount = 0, totalTeacherCount = 0, totalReorderCount = 0, totalCount = 0,
                        totalTimeTable = 0;
                List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> items = center.getItems();

                for (int idx = 0; idx < items.size(); idx++) {
                    LogisRespDTO.CenterAggregateDTO.AggregateItemDTO item = items.get(idx);

                    if (isTeacherMode) {
                        boolean isFirst = idx == 0 || !items.get(idx - 1).getUserName().equals(item.getUserName());
                        if (isFirst) {
                            Row teacherRow = sheet.createRow(rowIdx++);
                            teacherRow.createCell(0).setCellValue(item.getUserName() + " 선생님");
                            for (int i = 1; i < colCount; i++)
                                teacherRow.createCell(i);
                            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, colCount - 1));
                            for (int i = 0; i < colCount; i++)
                                teacherRow.getCell(i).setCellStyle(teacherStyle);
                        }
                    }

                    Row row = sheet.createRow(rowIdx++);
                    int col = 0;
                    if (isTeacherMode)
                        row.createCell(col++).setCellValue(item.getUserName());
                    row.createCell(col++).setCellValue(item.getClassName());
                    row.createCell(col++).setCellValue(item.getUnitName());
                    row.createCell(col++).setCellValue(item.getBaseCount());
                    row.createCell(col++).setCellValue(item.getTeacherCount());
                    row.createCell(col++).setCellValue(item.getReorderCount());
                    row.createCell(col++).setCellValue(item.getTotalCount());
                    if (!isAllMode)
                        row.createCell(col++).setCellValue(item.getTimeTableCount());

                    for (int i = 0; i < colCount; i++)
                        row.getCell(i).setCellStyle(cellStyle);

                    totalBaseCount += item.getBaseCount();
                    totalTeacherCount += item.getTeacherCount();
                    totalReorderCount += item.getReorderCount();
                    totalCount += item.getTotalCount();
                    totalTimeTable += item.getTimeTableCount();

                    if (isTeacherMode) {
                        boolean isLast = idx == items.size() - 1
                                || !items.get(idx + 1).getUserName().equals(item.getUserName());
                        if (isLast) {
                            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> sameTeacher = items.stream()
                                    .filter(i -> i.getUserName().equals(item.getUserName()))
                                    .toList();

                            int tBase = sameTeacher.stream()
                                    .mapToInt(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO::getBaseCount).sum();
                            int tTeacher = sameTeacher.stream()
                                    .mapToInt(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO::getTeacherCount).sum();
                            int tReorder = sameTeacher.stream()
                                    .mapToInt(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO::getReorderCount).sum();
                            int tTotal = sameTeacher.stream()
                                    .mapToInt(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO::getTotalCount).sum();
                            int tTime = sameTeacher.stream()
                                    .mapToInt(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO::getTimeTableCount)
                                    .sum();

                            Row subTotalRow = sheet.createRow(rowIdx++);
                            subTotalRow.createCell(0).setCellValue(item.getUserName() + " 합계");
                            subTotalRow.createCell(1);
                            subTotalRow.createCell(2);
                            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2));
                            subTotalRow.createCell(3).setCellValue(tBase);
                            subTotalRow.createCell(4).setCellValue(tTeacher);
                            subTotalRow.createCell(5).setCellValue(tReorder);
                            subTotalRow.createCell(6).setCellValue(tTotal);
                            if (!isAllMode)
                                subTotalRow.createCell(7).setCellValue(tTime);

                            for (int i = 0; i < colCount; i++)
                                subTotalRow.getCell(i).setCellStyle(subTotalStyle);
                        }
                    }
                }

                Row totalRow = sheet.createRow(rowIdx++);
                int labelSpan = isTeacherMode ? 3 : 2;
                totalRow.createCell(0).setCellValue(center.getCenterName() + " 합계");
                for (int i = 1; i < labelSpan; i++)
                    totalRow.createCell(i);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, labelSpan - 1));
                totalRow.createCell(labelSpan).setCellValue(totalBaseCount);
                totalRow.createCell(labelSpan + 1).setCellValue(totalTeacherCount);
                totalRow.createCell(labelSpan + 2).setCellValue(totalReorderCount);
                totalRow.createCell(labelSpan + 3).setCellValue(totalCount);
                if (!isAllMode)
                    totalRow.createCell(labelSpan + 4).setCellValue(totalTimeTable);

                for (int i = 0; i < colCount; i++)
                    totalRow.getCell(i).setCellStyle(totalStyle);
            }

            for (int i = 0; i < colCount; i++)
                sheet.setColumnWidth(i, 4500);

            String fileName = URLEncoder.encode(segmentType + "_" + year + month + ".xlsx",
                    StandardCharsets.UTF_8).replace("+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            workbook.write(response.getOutputStream());
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    @GetMapping("/center/timeview")
    public String getAdminTimeViewPage(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("year", String.valueOf(now.getYear()));
        model.addAttribute("month", String.format("%02d", now.getMonthValue()));
        model.addAttribute("center", centerService.findAllCenter());
        return "/admin/center/center-timeview";
    }

    @GetMapping("/center/print-timeview")
    public String getAdminPrintTimeView(
            @RequestParam("centerCode") String centerCode,
            @RequestParam("userCode") String userCode,
            @RequestParam("ym") String ym,
            Model model) {
        String yy = ym.substring(0, 4);
        String mm = ym.substring(4, 6);

        List<User> teachers = userService.findActiveUserByCenterCode(centerCode);
        User selectedUser = teachers.stream()
                .filter(u -> userCode.equals(u.getUserCode()))
                .findFirst()
                .orElse(null);

        ClassRespDTO.TimeTableViewRespDTO viewData = classService.findTableViewWithStudents(yy, mm, userCode, centerCode);

        Map<String, Map<String, TimeTableDTO>> tableMap = viewData.getTables().stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO::getDayname,
                        Collectors.toMap(TimeTableDTO::getPeriodNo, Function.identity())));
        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));

        Map<String, List<ClassRespDTO.StudentStatRespDTO>> statsMap = viewData.getStats().stream()
                .collect(Collectors.groupingBy(ClassRespDTO.StudentStatRespDTO::getGb));

        model.addAttribute("yy", yy);
        model.addAttribute("mm", mm);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("days", DAYS);
        model.addAttribute("tableMap", tableMap);
        model.addAttribute("statsMap", statsMap);
        model.addAttribute("totalStudentsLong", viewData.getTotalStudentsLong());
        model.addAttribute("totalStudentsDouble", viewData.getTotalStudentsDouble());

        return "print/print-timeview";
    }

    @GetMapping("/ebook/keycode")
    public String getAdminKeycodePage(Model model, HttpSession session) {
        List<Center> center = centerService.findBranchCenter();
        model.addAttribute("center", center);
        return "/admin/ebook/keycode";
    }

    @GetMapping("/ebook/person")
    public String getAdminPersonPage(Model model, HttpSession session) {
        List<UnitCode> unitCodes = classService.findUnitCodeForPerson();
        List<Center> center = centerService.findBranchCenter();
        model.addAttribute("unitCodes", unitCodes);
        model.addAttribute("center", center);
        return "/admin/ebook/person";
    }

    @GetMapping("/app/record")
    public String getAdminRecordPage(Model model, HttpSession session) {
        return "/admin/app/record";
    }

    @GetMapping("/app/infant")
    public String getAdminInfantPage(Model model, HttpSession session) {
        return "/admin/app/infant";
    }

    @GetMapping("/app/monthly")
    public String getAdminMonthlyPage(Model model, HttpSession session) {
        return "/admin/app/monthly";
    }

    @GetMapping("/app/book")
    public String book(Model model, HttpSession session) {
        List<ClassCode> classCodes = classService.findClassCodeExcludeMid();
        List<SubjectCode> subjects = adminService.findSubjects();
        List<AdminRespDTO.BookSuggestViewDTO> list = adminService.findBookSuggest();

        // week 기준 Map (1~4)
        Map<Integer, AdminRespDTO.BookSuggestViewDTO> bookMap = list.stream()
                .collect(Collectors.toMap(
                        b -> Integer.parseInt(b.getWeek()),
                        b -> b,
                        (a, b) -> a));
        model.addAttribute("subjects", subjects);
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("bookMap", bookMap);
        return "admin/app/book-suggest";
    }
}
