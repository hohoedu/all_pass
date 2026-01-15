package com.hohoedu.all_pass.notice;

import com.google.type.DateTime;
import com.hohoedu.all_pass._core.firebase.FcmService;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppReqDTO;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppRespDTO;
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
import java.util.Locale;

import static com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO.CenterNoticeDTO.sanitizeHtml;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final FcmService fcmService;

    public int insertCenterNotice(NoticeReqDTO.CenterNoticeSaveReqDTO dto, UserRespDTO.LoginRespDTO user) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        dto.setCenterNoticeKey(user.getUserCode() + now.format(formatter));
        dto.setCenterCode(user.getCenterCode());
        dto.setUserCode(user.getUserCode());
        dto.setViewCount(0);
        int result = noticeRepository.insertCenterNotice(dto);

        if (dto.getTokens() != null && !dto.getTokens().isEmpty()) {
            // HTML 태그 제거
            String message = dto.getContent().replaceAll("<[^>]*>", "");
            // 100자 제한
            if (message.length() > 100) {
                message = message.substring(0, 100) + "...";
            }

            // 선택된 학생들에게 알림 발송
            for (String token : dto.getTokens()) {
                try {
                    fcmService.sendMessage(token, dto.getTitle(), message);
                } catch (Exception e) {
                    // 개별 토큰 실패 시 로그만 남기고 계속 진행
                    System.err.println("FCM 발송 실패 (token: " + token + "): " + e.getMessage());
                }
            }
        }

        return result;
    }

    public List<NoticeRespDTO.CenterNoticeDTO> findCenterNoticeByCenterCode(UserRespDTO.LoginRespDTO user) {


        List<NoticeRespDTO.CenterNoticeDTO> noticeList = noticeRepository.findCenterNoticeByCenterCode(user.getCenterCode())
                .stream()
                .peek(dto -> dto.setCleanContent(sanitizeHtml(dto.getRawContent())))
                .toList();
        return noticeList;

    }

    public List<NoticeRespDTO.NoticeStudentDTO> findStudentByUserCode(UserRespDTO.LoginRespDTO user) {
        return noticeRepository.findStudentByUserCode(user.getUserCode(), user.getCenterCode());
    }

    public NoticeRespDTO.CenterNoticeDetailDTO findCenterNoticeByNoticeId(UserRespDTO.LoginRespDTO user, Integer id) {

        NoticeRespDTO.CenterNoticeDetailDTO notice = noticeRepository.findCenterNoticeByNoticeId(user.getCenterCode(), id);
        notice.setCleanContent(sanitizeHtml(notice.getRawContent()));
        return notice;
    }

    // ============================== app ============================== //

    public List<NoticeAppRespDTO.NoticeListRespDTO> findAppNoticeList(NoticeAppReqDTO.NoticeAppListReqDTO reqDTO) {
        String studentId = reqDTO.getStudentId();
        Integer count = reqDTO.getCount();
        DateTimeFormatter oldFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd a h:mm:ss", Locale.KOREAN);

        List<NoticeAppRespDTO.NoticeListRespDTO> noticeList = noticeRepository.findAppNoticeList(studentId, count);

        noticeList.stream().peek(n -> {
            String sdate = n.getSdate();

            if (sdate != null) {
                LocalDateTime date = LocalDateTime.parse(sdate, oldFormatter);
                n.setSdate(date.format(newFormatter)
                );
            }
        }).toList();

        return noticeList;
    }

    public NoticeAppRespDTO.NoticeDetailRespDTO findAppNoticeDetail(Integer id) {

        NoticeAppRespDTO.NoticeDetailRespDTO noticeDetail = noticeRepository.findAppNoticeDetail(id);
        System.out.println(noticeDetail);

        return noticeDetail;

    }
}
