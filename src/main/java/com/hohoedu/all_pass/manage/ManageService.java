package com.hohoedu.all_pass.manage;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.repository.CenterJpaRepository;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.manage.repository.ManageRepository;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.popbill.PopbillService;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.hohoedu.all_pass.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManageService {

    private final ManageRepository manageRepository;
    private final PopbillService popbillService;
    private final ClassService classService;
    private final UserRepository userRepository;
    private final CenterJpaRepository centerJpaRepository;

    public List<ManageRespDTO.BasicOrderListDTO> getBasicOrderList(String userCode, String centerCode, String year,
                                                                   String month) {

        List<ManageRespDTO.BasicOrderListDTO> orderListDTO = manageRepository.findBasicOrderList(centerCode, userCode,
                year, month);

        return orderListDTO;
    }

    public List<ManageRespDTO.SavedOrderListDTO> getSavedOrderList(String userCode, String centerCode, String year,
                                                                   String month) {

        List<ManageRespDTO.SavedOrderListDTO> orderListDTO = manageRepository.findSavedOrderList(centerCode, userCode,
                year, month);

        return orderListDTO;
    }

    @Transactional
    public void insertOrder(ManageReqDTO.InsertOrderHistoryDTO reqDTO) {

        // 1) order_history insert (무조건 insert)
        manageRepository.insertOrderHistory(reqDTO);

        // 2) order 조회
        List<ManageRespDTO.BaseOrderListDTO> baseOrderList = manageRepository.findOrder(reqDTO);
        log.info(baseOrderList.toString());

        // 3) 있으면 딜리트
        if (!baseOrderList.isEmpty()) {
            manageRepository.deleteOrderByCondition(reqDTO.getUserCode(), reqDTO.getCenterCode(), reqDTO.getYy(),
                    reqDTO.getMm());

        }
        // 4) 없으면 인서트
        manageRepository.insertOrder(reqDTO);

    }

    public List<ManageRespDTO.ReorderListDTO> getReorderList(String userCode, String centerCode, String year,
                                                             String month) {

        List<ManageRespDTO.ReorderListDTO> reorderListDTO = manageRepository.findReorderList(centerCode, userCode, year,
                month);
        reorderListDTO.stream()
                .peek(dto -> {
                    if (dto.getCreatedAt() != null) {
                        dto.setCreatedAt(
                                LocalDateTime.parse(
                                        dto.getCreatedAt(),
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")).format(
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                })
                .collect(Collectors.toList());

        return reorderListDTO;

    }

    public List<ManageRespDTO.OrderDetailDTO> getOrderDetailList(String userCode, String centerCode, String year,
                                                                 String month) {

        List<ManageRespDTO.OrderDetailDTO> reorderListDTO = manageRepository.findOrderDetailList(centerCode, userCode,
                year, month);

        return reorderListDTO;

    }

    public List<ManageRespDTO.TeacherOrderGroupDTO> getCenterOrderList(String ym, String centerCode, String userCode) {
        List<ManageRespDTO.CenterOrderListDTO> flatList = manageRepository.findCenterOrderList(ym, centerCode,
                userCode);

        Map<String, List<ManageRespDTO.CenterOrderListDTO>> grouped = flatList.stream()
                .collect(Collectors.groupingBy(
                        ManageRespDTO.CenterOrderListDTO::getUserName,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream().map(entry -> {
            ManageRespDTO.TeacherOrderGroupDTO g = new ManageRespDTO.TeacherOrderGroupDTO();
            g.setUserName(entry.getKey());
            g.setRows(entry.getValue());
            g.setSumStudent(
                    entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getStudentCount).sum());
            g.setSumTeacher(
                    entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getTeacherCount).sum());
            g.setSumAdd(entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getAddCount).sum());
            g.setSumTotal(entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getTotalCount).sum());
            g.setSumTimeTable(entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getTimeTable).sum());
            return g;
        }).collect(Collectors.toList());
    }

    public List<ManageRespDTO.CenterOrderListDTO> getCenterOrderListFlat(String ym, String centerCode,
                                                                         String userCode) {
        return manageRepository.findCenterOrderList(ym, centerCode, userCode);
    }

    public String getOrderDeadline(String centerCode) {
        String result = manageRepository.findOrderDeadline(centerCode);
        return result;
    }

    public int insertReorder(ManageReqDTO.InsertReorderDTO req, UserRespDTO.LoginRespDTO user) {

        String yy = req.getYy();
        String mm = req.getMm();

        String userCode = user.getUserCode();
        String centerCode = user.getCenterCode();

        for (ManageReqDTO.InsertReorderDTO.ReorderItemDTO item : req.getItems()) {
            manageRepository.insertReorder(userCode, centerCode, yy, mm, req.getReorderType(), item.getClassKey(),
                    item.getUnitKey(), item.getCount(), item.getReason());
        }
        processAddReorder(req, user);

        return 1;
    }

    private void processAddReorder(ManageReqDTO.InsertReorderDTO req, UserRespDTO.LoginRespDTO user) {
        try {

            String orderContent = buildOrderContent(req.getReorderType(), req.getItems());

            popbillService.sendAddReorderAlimtalk(
                    user,
                    orderContent);

            log.info("추가 주문 알림톡 발송 완료");

        } catch (Exception e) {
            log.error("추가 주문 알림톡 발송 실패 - error: {}", e.getMessage(), e);
        }
    }

    private String buildOrderContent(String reorderType, List<ManageReqDTO.InsertReorderDTO.ReorderItemDTO> items) {
        List<Map<String, String>> classResults = manageRepository.findAllClassNames();
        Map<String, String> classNameMap = classResults.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("classKey"),
                        map -> (String) map.get("className")));

        List<Map<String, String>> unitResults = manageRepository.findAllUnitNames();
        Map<String, String> unitNameMap = unitResults.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("unitKey"),
                        map -> (String) map.get("unitName")));
        String typeLabel = "ADD".equals(reorderType) ? "추가 주문" : "반품";

        // 2. 주문 내용 생성
        StringBuilder content = new StringBuilder();

        content.append("(").append(typeLabel).append(")\n");

        for (int i = 0; i < items.size(); i++) {
            ManageReqDTO.InsertReorderDTO.ReorderItemDTO item = items.get(i);

            String className = classNameMap.get(item.getClassKey());
            String unitName = unitNameMap.get(item.getUnitKey());

            content.append(className)
                    .append(" ")
                    .append(unitName)
                    .append(" ")
                    .append(item.getCount())
                    .append("권");

            if (i < items.size() - 1) {
                content.append(",\n");
            }
        }

        return content.toString();
    }

    public String cancelReorder(Integer id) {
        int result = manageRepository.cancelReorder(id);
        if (result > 0) {
            return "0000";
        }
        return "9999";
    }

    // 센터별 수업료 조회
    public List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(String centerCode) {
        List<PaymentRespDTO.ClassFeeMapDTO> classFeeMaps = manageRepository.findClassFeeMapByCenterCode(centerCode);
        return classFeeMaps;
    }

    public int insertClassFeeMap(List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList, String centerCode) {
        List<PaymentRespDTO.ClassFeeMapDTO> classFeeMaps = manageRepository.findClassFeeMapByCenterCode(centerCode);
        log.info("classFeeMaps = {}", classFeeMaps);
        int resp = 0;
        if (classFeeMaps == null || classFeeMaps.isEmpty()) {
            resp = manageRepository.insertClassFeeMap(feeMapList);
        } else {
            resp = manageRepository.updateClassFeeMap(feeMapList);
        }
        return resp;
    }

    // 수업료 수정
    public int updateClassFeeMap(List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList) {
        return manageRepository.updateClassFeeMap(feeMapList);
    }

    public ManageRespDTO.TuitionRespDTO getTuitionData(String centerCode) {
        List<ClassCode> classCodes = classService.findClassCode();
        List<PaymentRespDTO.ClassFeeMapDTO> feeMaps = findClassFeeMapByCenterCode(centerCode);

        return ManageRespDTO.TuitionRespDTO.builder()
                // 클래스 목록 (category별로 조회)
                .hohoClasses(getClassesByCategory(classCodes, feeMaps, "hoho"))
                .hanClasses(getClassesByCategory(classCodes, feeMaps, "han"))
                .bookClasses(getClassesByCategory(classCodes, feeMaps, "book"))

                // Fee 매핑 (category별로 조회)
                .hohoFeeMap(getFeeMapByCategory(feeMaps, "hoho"))
                .hanFeeMap(getFeeMapByCategory(feeMaps, "han"))
                .bookFeeMap(getFeeMapByCategory(feeMaps, "book"))
                .build();
    }

    // category에 해당하는 classKey들을 추출해서 해당하는 ClassCode 반환
    private List<ClassCode> getClassesByCategory(
            List<ClassCode> classCodes,
            List<PaymentRespDTO.ClassFeeMapDTO> feeMaps,
            String category) {

        Set<String> classKeys = feeMaps.stream()
                .filter(f -> category.equalsIgnoreCase(f.getCategory()))
                .map(PaymentRespDTO.ClassFeeMapDTO::getClassKey)
                .collect(Collectors.toSet());

        return classCodes.stream()
                .filter(c -> classKeys.contains(c.getClassKey()))
                .toList();
    }

    // category별 fee 매핑
    private Map<String, String> getFeeMapByCategory(
            List<PaymentRespDTO.ClassFeeMapDTO> feeMaps,
            String category) {

        return feeMaps.stream()
                .filter(f -> category.equalsIgnoreCase(f.getCategory()))
                .collect(Collectors.toMap(
                        PaymentRespDTO.ClassFeeMapDTO::getClassKey,
                        PaymentRespDTO.ClassFeeMapDTO::getFee));
    }

    public ManageRespDTO.TeacherDetailDTO findUserByUserCode(String userCode) {

        ManageRespDTO.TeacherDetailDTO detail = manageRepository.findUserByUserCode(userCode);

        detail.setMenuPermissions(manageRepository.findMenuAuthByUserCode(userCode));

        return detail;

    }

    @Transactional
    public void savePermission(String userCode, ManageReqDTO.PermissionReqDTO permission) {

        manageRepository.deletePermissionByUserCode(userCode);

        if (!permission.getPermissions().isEmpty()) {
            manageRepository.insertPermissions(userCode, permission.getPermissions());
        }

    }

    public List<String> getReadableMenus(String userCode) {
        return userRepository.findReadableMenus(userCode);
    }

    public void copyUserPermission(String sourceUserCode, String targetUserCode) {
        manageRepository.copyUserPermission(sourceUserCode, targetUserCode);
    }

    public void processOrderIfScheduled() {
        int today = LocalDate.now().getDayOfMonth();

        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        String yy = String.valueOf(nextMonth.getYear());
        String mm = String.format("%02d", nextMonth.getMonthValue());


        // 1. 센터코드 조회
        // (모든센터)
//        List<Center> centerList = centerJpaRepository.findAll();
        List<Center> centerList = centerJpaRepository.findAll()
                .stream()
//                // dev
//                .filter(c -> "PUS001".equals(c.getCenterCode()))
//                // prod
//                .filter(c -> !"PUS001".equals(c.getCenterCode()))
                .collect(Collectors.toList());

        int count = 0;
        for (Center center : centerList) {
            String centerCode = center.getCenterCode();
            String targetDay = manageRepository.findOrderDeadline(centerCode);
            if (targetDay == null) {
                log.info("주문일이 없습니다.");
                continue;
            }
            if (today != Integer.parseInt(targetDay)) {
                log.info("주문일 아님 - 오늘: {}일, 주문일: {}일", today, targetDay);
                continue;
            }
            // 2. 센터별 유저코드 조회
            List<User> userList = userRepository.findAllUserCode(centerCode);
            // 사용중인 모든 선생님
            userList = userList.stream()
                    .filter(u -> Boolean.TRUE.equals(u.getIsHan()) || Boolean.TRUE.equals(u.getIsBook()))
                    .collect(Collectors.toList());
            // 북스쿨 선생님만
//            userList = userList.stream()
//                    .filter(u -> Boolean.TRUE.equals(u.getIsBook()) && Boolean.FALSE.equals(u.getIsHan()))
//                    .collect(Collectors.toList());

            for (User user : userList) {

                String userCode = user.getUserCode();

                log.info("{} userCode = {}", count, userCode);

                List<ManageRespDTO.BasicOrderListDTO> classList = manageRepository.findBasicOrderList(centerCode, userCode, yy, mm);

                if (classList.isEmpty()) {
                    continue;
                }

                List<ManageReqDTO.InsertOrderHistoryDTO.InsertOrder> insertOrders = classList.stream()
                        .map(c -> {
                            ManageReqDTO.InsertOrderHistoryDTO.InsertOrder order = new ManageReqDTO.InsertOrderHistoryDTO.InsertOrder();
                            order.setClassKey(c.getClassKey());
                            order.setUnitKey(c.getUnitKey());
                            order.setBaseCount(c.getBaseCount());
                            order.setAddCount(0);
                            order.setTotalCount(c.getBaseCount());
                            return order;
                        })
                        .collect(Collectors.toList());

                ManageReqDTO.InsertOrderHistoryDTO reqDTO = new ManageReqDTO.InsertOrderHistoryDTO();
                reqDTO.setCenterCode(centerCode);
                reqDTO.setUserCode(userCode);
                reqDTO.setYy(yy);
                reqDTO.setMm(mm);
                reqDTO.setInsertOrders(insertOrders);

                manageRepository.insertOrderHistory(reqDTO);

                List<ManageRespDTO.BaseOrderListDTO> baseOrderList = manageRepository.findOrder(reqDTO);

                if (!baseOrderList.isEmpty()) {
                    manageRepository.deleteOrderByCondition(reqDTO.getUserCode(), reqDTO.getCenterCode(), reqDTO.getYy(),
                            reqDTO.getMm());

                }
                manageRepository.insertOrder(reqDTO);
            }

        }
    }
}
