package com.hohoedu.all_pass.notice;

import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import com.hohoedu.all_pass.notice.repository.NoticeRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public int insertCenterNotice(NoticeReqDTO.CenterNoticeSaveReqDTO dto, UserRespDTO.LoginRespDTO user) {
        dto.setCenterCode(user.getCenterCode());
        dto.setUserCode(user.getUserCode());
        dto.setViewCount(0);
        int result = noticeRepository.insertCenterNotice(dto);
        return result;
    }
}
