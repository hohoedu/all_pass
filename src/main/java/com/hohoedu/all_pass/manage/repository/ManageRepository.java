package com.hohoedu.all_pass.manage.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ManageRepository {
    // viewController 주문서 조회
    List<ManageRespDTO.BasicOrderListDTO> findBasicOrderList(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    List<ManageRespDTO.SavedOrderListDTO> findSavedOrderList(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    List<ManageRespDTO.ReorderListDTO> findReorderList(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);


    // 정규 주문서 로직

    // order_history insert
    void insertOrderHistory(ManageReqDTO.InsertOrderHistoryDTO dto);

    // 주문서 조회
    List<ManageRespDTO.BaseOrderListDTO> findOrder(ManageReqDTO.InsertOrderHistoryDTO dto);

    void insertOrder(ManageReqDTO.InsertOrderHistoryDTO dto);

    void deleteOrderByCondition(
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm
            );

    int insertReorder(
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("reorderType") String reorderType,
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("count") Integer count,
            @Param("reason") String reason);

    // 학원별 수강료 조회
    List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(@Param("centerCode") String centerCode);

    int insertClassFeeMap(@Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList);

    // 학원별 수강료 수정
    int updateClassFeeMap(@Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList);

    // 추가: 전체 class 이름 조회
    List<Map<String, String>> findAllClassNames();

    // 추가: 전체 unit 이름 조회
    List<Map<String, String>> findAllUnitNames();

    int cancelReorder(Integer id);
}
