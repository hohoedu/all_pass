package com.hohoedu.all_pass.manage;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.manage.repository.ManageRepository;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManageService {

    private final ManageRepository manageRepository;

    public List<ManageRespDTO.BasicOrderListDTO> getBasicOrderList(String userCode, String centerCode, String year, String month) {

        List<ManageRespDTO.BasicOrderListDTO> orderListDTO = manageRepository.findBasicOrderList(centerCode, userCode, year, month);

        return orderListDTO;
    }

    public List<ManageRespDTO.SavedOrderListDTO> getSavedOrderList(String userCode, String centerCode, String year, String month) {

        List<ManageRespDTO.SavedOrderListDTO> orderListDTO = manageRepository.findSavedOrderList(centerCode, userCode, year, month);

        return orderListDTO;
    }

    public int insertReorder(ManageReqDTO.InsertReorderDTO req, UserRespDTO.LoginRespDTO user) {
        LocalDate now = LocalDate.now();
        String yy = String.valueOf(now.getYear());
        String mm = String.format("%02d", now.getMonthValue());

        String userCode = user.getUserCode();
        String centerCode = user.getCenterCode();

        for (ManageReqDTO.InsertReorderDTO.ReorderItemDTO item : req.getItems()) {
            log.info(req.getReorderType());
            manageRepository.insertReorder(userCode, centerCode, yy, mm, req.getReorderType(), item.getClassKey(), item.getUnitKey(), item.getCount(), item.getReason());
        }
        return 1;
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

    public void insertOrder(List<ManageReqDTO.InsertOrderDTO> reqDTO) {
        for (ManageReqDTO.InsertOrderDTO dto : reqDTO) {

            ManageReqDTO.InsertOrderDTO existing =
                    manageRepository.findOrder(dto.getCenterCode(),
                            dto.getClassKey(),
                            dto.getUnitKey(),
                            dto.getUserCode(),
                            dto.getYy(),
                            dto.getMm());

            // 1) 신규 주문 (없으면 insert)
            if (existing == null) {
                dto.setTotalCount(dto.getBaseCount() + dto.getAddCount());
                manageRepository.insertOrder(dto);
            }

            // 2) 기존 주문 수정 (있으면 update)
            else {
                dto.setTotalCount(dto.getBaseCount() + dto.getAddCount());
                manageRepository.updateOrder(dto);
            }
        }

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


}
