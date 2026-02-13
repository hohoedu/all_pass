package com.hohoedu.all_pass.notice.repository;

import com.hohoedu.all_pass.notice.CenterNotice;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppRespDTO;
import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface NoticeRepository {
    int insertCenterNotice(NoticeReqDTO.CenterNoticeSaveReqDTO dto);

    int updateCenterNotice(NoticeReqDTO.CenterNoticeUpdateDTO noticeDTO);
    int deleteCenterNotice(Integer id);

    List<NoticeRespDTO.CenterNoticeDTO> findCenterNoticeByCenterCode(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode);

    NoticeRespDTO.CenterNoticeDetailDTO findCenterNoticeByNoticeId(
            @Param("centerCode") String centerCode, Integer id);

    List<NoticeAppRespDTO.NoticeListRespDTO> findAppNoticeList(
            @Param("studentId") String studentId,
            @Param("count") Integer count);

    NoticeAppRespDTO.NoticeDetailRespDTO findAppNoticeDetail(@Param("id") Integer id);

    List<NoticeRespDTO.NoticeStudentDTO> findStudentByUserCode(String userCode, String centerCode);
}
