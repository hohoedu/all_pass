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

import java.util.List;

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

    public void insertOrder(List<ManageReqDTO.InsertOrderDTO> reqDTO) {
        for (ManageReqDTO.InsertOrderDTO dto : reqDTO) {

            ManageReqDTO.InsertOrderDTO existing =
                    manageRepository.findOrder(dto.getCenterCode(),
                            dto.getClassKey(),
                            dto.getUnitKey(),
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
