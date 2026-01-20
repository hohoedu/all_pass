package com.hohoedu.all_pass.consult.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsultRepository {

    public void registerConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);

    public List<ConsultRespDTO.ConsultDTO> findAll(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode
    );

    public List<ConsultRespDTO.ConsultDTO> findByPeriod(Map<String, Object> params);

    void deleteByIds(List<Integer> ids);

    void updateProgress(@Param("id") Integer id, @Param("progressKey") String progressKey);

    void updateConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);
}
