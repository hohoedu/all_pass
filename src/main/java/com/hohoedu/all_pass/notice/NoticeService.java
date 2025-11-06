package com.hohoedu.all_pass.notice;

import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO;
import com.hohoedu.all_pass.notice.repository.NoticeRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO.CenterNoticeDTO.sanitizeHtml;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public int insertCenterNotice(NoticeReqDTO.CenterNoticeSaveReqDTO dto, UserRespDTO.LoginRespDTO user) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        dto.setCenterNoticeKey(user.getUserCode() + now.format(formatter));
        dto.setCenterCode(user.getCenterCode());
        dto.setUserCode(user.getUserCode());
        dto.setViewCount(0);
        int result = noticeRepository.insertCenterNotice(dto);
        return result;
    }

    public List<NoticeRespDTO.CenterNoticeDTO> findCenterNoticeByCenterCode(UserRespDTO.LoginRespDTO user) {


        List<NoticeRespDTO.CenterNoticeDTO> noticeList = noticeRepository.findCenterNoticeByCenterCode(user.getCenterCode())
                .stream()
                .peek(dto -> dto.setCleanContent(sanitizeHtml(dto.getRawContent())))
                .toList();
        return noticeList;

    }

    public NoticeRespDTO.CenterNoticeDetailDTO findCenterNoticeByNoticeId(UserRespDTO.LoginRespDTO user, Integer id) {

        NoticeRespDTO.CenterNoticeDetailDTO notice = noticeRepository.findCenterNoticeByNoticeId(user.getCenterCode(), id);
        notice.setCleanContent(sanitizeHtml(notice.getRawContent()));
        return notice;
    }
}
