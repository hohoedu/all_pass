package com.hohoedu.all_pass.popbill.repository;

import com.hohoedu.all_pass.popbill.PopbillConfig;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.popbill._dto.PopbillRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PopbillRepository {

    int insertPopbillConfig(PopbillConfig popbillInsertReqDTO);

    PopbillConfig findPopbillConfig(String centerCode);

    PopbillRespDTO.PopbillTemplateRespDTO findPopbillTemplate(
            @Param("centerCode") String centerCode,
            @Param("category") String category

    );

    int insertSendLog(PopbillReqDTO.PopbillSendLogReqDTO dto);

    int insertInviteTracking(PopbillReqDTO.InviteTrackingReqDTO dto);
}
