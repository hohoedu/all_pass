package com.hohoedu.all_pass.center.repository;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.center._dto.CenterRespDTO.PaymintConfigDTO;

@Mapper
public interface CenterRepository {

    PaymintConfigDTO findPaymintConfigByCenterCode(String centerCode);
}
