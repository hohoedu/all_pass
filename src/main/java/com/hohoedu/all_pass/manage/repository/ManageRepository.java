package com.hohoedu.all_pass.manage.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManageRepository {

    // 학원별 수강료 조회
    List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(@Param("centerCode") String centerCode);

    // 학원별 수강료 수정
    int updateClassFeeMap(@Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList);

}
