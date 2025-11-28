package com.hohoedu.all_pass.manage.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManageRepository {
    // viewController 주문서 조회
    List<ManageRespDTO.BasicOrderListDTO> findBasicOrderList(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    // 주문서 조회
    ManageReqDTO.InsertOrderDTO findOrder(
            @Param("centerCode") String centerCode,
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("yy") String yy,
            @Param("mm") String mm);

    // 주문서 최초 저장
    void insertOrder(ManageReqDTO.InsertOrderDTO dto);

    // 주문서 업데이트
    void updateOrder(ManageReqDTO.InsertOrderDTO dto);

    // 학원별 수강료 조회
    List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(@Param("centerCode") String centerCode);

    // 학원별 수강료 수정
    int updateClassFeeMap(@Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList);


}
