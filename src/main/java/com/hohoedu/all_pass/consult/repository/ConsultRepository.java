package com.hohoedu.all_pass.consult.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;

@Mapper
public interface ConsultRepository {

    public void registerConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO);

    public List<ConsultRespDTO.ConsultDTO> findAll();

}
