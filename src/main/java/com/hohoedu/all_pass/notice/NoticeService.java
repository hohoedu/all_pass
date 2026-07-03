package com.hohoedu.all_pass.notice;

import com.hohoedu.all_pass._core.firebase.FcmService;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppReqDTO;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppRespDTO;
import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO;
import com.hohoedu.all_pass.notice.repository.NoticeRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO.CenterNoticeDTO.sanitizeHtml;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final FcmService fcmService;

    public Map<String, Object> insertCenterNotice(NoticeReqDTO.CenterNoticeSaveReqDTO dto,
            UserRespDTO.LoginRespDTO user) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        dto.setCenterNoticeKey(user.getUserCode() + now.format(formatter));
        dto.setCenterCode(user.getCenterCode());
        dto.setUserCode(user.getUserCode());
        dto.setViewCount(0);
        noticeRepository.insertCenterNotice(dto);

        if (dto.getStudentIds() != null && !dto.getStudentIds().isEmpty()) {
            for (String studentId : dto.getStudentIds()) {
                noticeRepository.insertRemedialNoticeMap(dto.getCenterNoticeKey(), studentId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (dto.getTokens() != null && !dto.getTokens().isEmpty()) {
            // HTML 태그 제거
            String message = dto.getContent().replaceAll("<[^>]*>", "");
            // 100자 제한
            if (message.length() > 100) {
                message = message.substring(0, 100) + "...";
            }

            // 선택된 학생들에게 비동기 알림 발송 (진행률은 progressKey로 조회)
            fcmService.sendMessagesAsync(dto.getCenterNoticeKey(), dto.getTokens(), dto.getTitle(), message);
            result.put("progressKey", dto.getCenterNoticeKey());
            result.put("total", dto.getTokens().size());
        }

        return result;
    }

    public List<NoticeRespDTO.CenterNoticeDTO> findCenterNoticeByCenterCode(UserRespDTO.LoginRespDTO user, boolean myOnly) {
    String userCode = myOnly ? user.getUserCode() : null;
    return noticeRepository.findCenterNoticeByCenterCode(user.getCenterCode(), userCode)
            .stream()
            .peek(dto -> dto.setCleanContent(sanitizeHtml(dto.getRawContent())))
            .toList();
}

    public int updateCenterNotice(NoticeReqDTO.CenterNoticeUpdateDTO noticeDTO) {
        return noticeRepository.updateCenterNotice(noticeDTO);
    }

    public int deleteCenterNotice(Integer id) {
        return noticeRepository.deleteCenterNotice(id);
    }

    public List<NoticeRespDTO.NoticeStudentDTO> findStudentByUserCode(UserRespDTO.LoginRespDTO user) {
        return noticeRepository.findStudentByCenterCode(user.getCenterCode());
    }

    public List<NoticeRespDTO.ClassCodeDTO> findClassListByCenterCode(UserRespDTO.LoginRespDTO user) {
        return noticeRepository.findClassListByCenterCode(user.getCenterCode());
    }

    public NoticeRespDTO.CenterNoticeDetailDTO findCenterNoticeByNoticeId(UserRespDTO.LoginRespDTO user, Integer id) {

        NoticeRespDTO.CenterNoticeDetailDTO notice = noticeRepository.findCenterNoticeByNoticeId(user.getCenterCode(),
                id);
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
                n.setSdate(date.format(newFormatter));
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
