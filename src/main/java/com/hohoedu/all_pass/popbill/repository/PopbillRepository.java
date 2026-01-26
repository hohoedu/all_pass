package com.hohoedu.all_pass.popbill.repository;

import com.hohoedu.all_pass.popbill.PopbillConfig;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.popbill._dto.PopbillRespDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PopbillRepository {

    int insertPopbillConfig(PopbillConfig popbillInsertReqDTO);

    PopbillConfig findPopbillConfig(String centerCode);

    PopbillRespDTO.PopbillTemplateRespDTO findPopbillTemplate(String centerCode);

    int insertSendLog(PopbillReqDTO.PopbillSendLogReqDTO dto);

    int insertInviteTracking(PopbillReqDTO.InviteTrackingReqDTO dto);
}
