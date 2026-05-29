package com.hohoedu.all_pass.consult.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsultRepository {

    void registerConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);

    List<ConsultRespDTO.ConsultDTO> findConsult(ConsultReqDTO.ConsultListReqDTO req);

    List<ConsultRespDTO.ConsultDTO> findByPeriod(Map<String, Object> params);

    void deleteByIds(List<Integer> ids);

    void updateProgress(@Param("id") Integer id, @Param("progressKey") String progressKey);

    int updateConsultContent(@Param("id") Integer id, @Param("content") String content);

    void updateConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);

    List<ConsultRespDTO.ConsultPrintDTO> findConsultForPrint(
            @Param("userCode") String userCode,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    String findUserNameByUserCode(String userCode);

    void updateSendKey(
            @Param("consultId") String consultId,
            @Param("sendKey") String sendKey
    );
}
