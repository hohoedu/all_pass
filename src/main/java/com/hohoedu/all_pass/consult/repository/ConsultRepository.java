package com.hohoedu.all_pass.consult.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsultRepository {

    String findConsultKeyByPhone(@Param("phone") String phone, @Param("centerCode") String centerCode);

    void registerConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);

    List<ConsultRespDTO.ConsultDTO> findConsult(ConsultReqDTO.ConsultListReqDTO req);

    List<ConsultRespDTO.ConsultModalRespDTO> findConsultModal(ConsultReqDTO.ConsultModalReqDTO req);

    List<ConsultRespDTO.ConsultDTO> findByPeriod(Map<String, Object> params);

    void deleteByIds(List<Integer> ids);

    void updateProgress(ConsultReqDTO.ConsultUpdateProgressReqDTO reqDTO);

    int updateConsultContent(@Param("id") Integer id, @Param("content") String content);

    void updateConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);

    List<ConsultRespDTO.ConsultPrintDTO> findConsultForPrint(
            @Param("userCode") String userCode,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<ConsultRespDTO.ConsultPrintDTO> findConsultForPrint(ConsultReqDTO.ConsultPrintReqDTO req);

    String findUserNameByUserCode(String userCode);

    void updateSendKey(
            @Param("consultId") String consultId,
            @Param("sendKey") String sendKey
    );

    boolean existsByConsultKey(String consultKey);


}
