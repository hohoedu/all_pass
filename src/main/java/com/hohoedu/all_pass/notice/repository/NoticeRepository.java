package com.hohoedu.all_pass.notice.repository;

import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeRepository {
    int insertCenterNotice(NoticeReqDTO.CenterNoticeSaveReqDTO dto);
}
