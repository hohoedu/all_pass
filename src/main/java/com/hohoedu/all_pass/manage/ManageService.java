package com.hohoedu.all_pass.manage;

import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.manage.repository.ManageRepository;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.popbill.PopbillService;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student.model.InviteTracking;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.hohoedu.all_pass.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
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

    public List<ManageRespDTO.BasicOrderListDTO> getBasicOrderList(String userCode, String centerCode, String year, String month) {

        List<ManageRespDTO.BasicOrderListDTO> orderListDTO = manageRepository.findBasicOrderList(centerCode, userCode, year, month);

        return orderListDTO;
    }

    public List<ManageRespDTO.SavedOrderListDTO> getSavedOrderList(String userCode, String centerCode, String year, String month) {

        List<ManageRespDTO.SavedOrderListDTO> orderListDTO = manageRepository.findSavedOrderList(centerCode, userCode, year, month);

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
            manageRepository.deleteOrderByCondition(reqDTO.getUserCode(), reqDTO.getCenterCode(), reqDTO.getYy(), reqDTO.getMm());

        }
        // 4) 없으면 인서트
        manageRepository.insertOrder(reqDTO);


    }

    public List<ManageRespDTO.ReorderListDTO> getReorderList(String userCode, String centerCode, String year, String month) {

        List<ManageRespDTO.ReorderListDTO> reorderListDTO = manageRepository.findReorderList(centerCode, userCode, year, month);
        reorderListDTO.stream()
                .peek(dto -> {
                    if (dto.getCreatedAt() != null) {
                        dto.setCreatedAt(
                                LocalDateTime.parse(
                                        dto.getCreatedAt(),
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                                ).format(
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                )
                        );
                    }
                })
                .collect(Collectors.toList());

        return reorderListDTO;

    }

    public List<ManageRespDTO.OrderDetailDTO> getOrderDetailList(String userCode, String centerCode, String year, String month) {

        List<ManageRespDTO.OrderDetailDTO> reorderListDTO = manageRepository.findOrderDetailList(centerCode, userCode, year, month);


        return reorderListDTO;

    }

    public List<ManageRespDTO.TeacherOrderGroupDTO> getCenterOrderList(String ym, String centerCode) {
        List<ManageRespDTO.CenterOrderListDTO> flatList = manageRepository.findCenterOrderList(ym, centerCode);

        // userName 기준 그룹핑 (SQL 정렬 순서 유지)
        Map<String, List<ManageRespDTO.CenterOrderListDTO>> grouped = flatList.stream()
                .collect(Collectors.groupingBy(
                        ManageRespDTO.CenterOrderListDTO::getUserName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream().map(entry -> {
            ManageRespDTO.TeacherOrderGroupDTO g = new ManageRespDTO.TeacherOrderGroupDTO();
            g.setUserName(entry.getKey());
            g.setRows(entry.getValue());
            g.setSumStudent(entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getStudentCount).sum());
            g.setSumTeacher(entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getTeacherCount).sum());
            g.setSumAdd    (entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getAddCount).sum());
            g.setSumTotal  (entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getTotalCount).sum());
            g.setSumTimeTable(entry.getValue().stream().mapToInt(ManageRespDTO.CenterOrderListDTO::getTimeTable).sum());
            return g;
        }).collect(Collectors.toList());
    }

    public List<ManageRespDTO.CenterOrderListDTO> getCenterOrderListFlat(String ym, String centerCode) {
        return manageRepository.findCenterOrderList(ym, centerCode);
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
            manageRepository.insertReorder(userCode, centerCode, yy, mm, req.getReorderType(), item.getClassKey(), item.getUnitKey(), item.getCount(), item.getReason());
        }
        processAddReorder(req, user);

        return 1;
    }

    private void processAddReorder(ManageReqDTO.InsertReorderDTO req, UserRespDTO.LoginRespDTO user) {
        try {

            String orderContent = buildOrderContent(req.getReorderType(), req.getItems());

            popbillService.sendAddReorderAlimtalk(
                    user,
                    orderContent
            );

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
                        map -> (String) map.get("className")
                ));

        List<Map<String, String>> unitResults = manageRepository.findAllUnitNames();
        Map<String, String> unitNameMap = unitResults.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("unitKey"),
                        map -> (String) map.get("unitName")
                ));
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
                        PaymentRespDTO.ClassFeeMapDTO::getFee
                ));
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
}
