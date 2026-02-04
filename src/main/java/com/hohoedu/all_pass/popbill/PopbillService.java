package com.hohoedu.all_pass.popbill;

import com.hohoedu.all_pass._core.utils.Aes256Util;
import com.hohoedu.all_pass._core.utils.PopbillServiceFactory;
import com.hohoedu.all_pass._core.utils.KeyGenerator;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.popbill._dto.PopbillRespDTO;
import com.hohoedu.all_pass.popbill.repository.PopbillRepository;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student.repository.StudentRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.popbill.api.KakaoService;
import com.popbill.api.PopbillException;
import com.popbill.api.kakao.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PopbillService {

    private final PopbillRepository popbillRepository;
    private final PopbillServiceFactory serviceFactory;
    private final KakaoService kakaoService;
    private final StudentRepository studentRepository;

    @Value("${popbill.aes.key}")
    private String aesKey;

    public void createPopbillConfig(PopbillReqDTO.PopbillInsertReqDTO reqDTO) {

        String encryptedKey = Aes256Util.encrypt(reqDTO.getSecretKey(), aesKey);

        PopbillConfig config = PopbillConfig.builder()
                .corpNumber(reqDTO.getCorpNumber())
                .linkId(reqDTO.getLinkId())
                .popbillId(reqDTO.getPopbillId())
                .encryptedKey(encryptedKey)
                .senderNumber(reqDTO.getSenderNumber())
                .center(Center.builder().centerCode(reqDTO.getCenterCode()).build())
                .build();

        int result = popbillRepository.insertPopbillConfig(config);

    }

    public String getPopbillAccessURL(String centerCode) {
        try {
            PopbillConfig config = popbillRepository.findPopbillConfig(centerCode);
//            KakaoService kakaoService = serviceFactory.getKakaoService(centerCode);

            // SSO URL 발급
            KakaoService kakaoService = serviceFactory.getKakaoService(centerCode);
            String url = kakaoService.getAccessURL(
                    config.getCorpNumber(),
                    config.getPopbillId()
            );

            log.info("팝빌 접속 URL 발급 성공: {}", url);
            return url;

        } catch (PopbillException e) {
            log.error("팝빌 URL 발급 실패 - code: {}, message: {}", e.getCode(), e.getMessage());
            throw new RuntimeException("팝빌 접속 URL 발급 실패: " + e.getMessage());
        }
    }

    public String sendJoinAlimtalk(String centerCode, String phone, String regionName, String centerName, String userCode) {
        String category = "join";
        try {

            KakaoService kakaoService = serviceFactory.getKakaoService(centerCode);

            /*
            * String CorpNum,         // 사업자 번호
            * String templateCode,    // 템플릿 코드
            * String senderNum,       // 발신 번호
            * String content,         // 내용
            * String altContent,      // 대체 내용
            * String altSendType,     // 타입
            * String receiverNum,     // 받는 사람 번호
            * String receiverName,    // 받는 사람 이름
            * String sndDT)           // 예약 일시
    }*/

            PopbillConfig config = popbillRepository.findPopbillConfig(centerCode);
            PopbillRespDTO.PopbillTemplateRespDTO respDTO = popbillRepository.findPopbillTemplate(centerCode, category);
            ATSTemplate template = kakaoService.getATSTemplate(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode()
            );

            log.info("=== 템플릿 정보 ===");
            log.info("템플릿명: {}", template.getTemplateName());
            log.info("템플릿 내용: {}", template.getTemplate());
            log.info("승인상태: {}", template.getState());
            log.info("등록일시: {}", template.getStateDT());

            String inviteCode = KeyGenerator.generateInviteCode();
            String sendKey = KeyGenerator.generateSendKey();
            String joinUrl = "https://hohocenter.co.kr/student/mobile/join?centerCode=" + centerCode + "&invite=" + inviteCode;
//            String joinUrl = "https://c5310b9e0f45.ngrok-free.app/student/mobile/join?centerCode=" + centerCode + "&invite=" + inviteCode;
            String androidUrl = "https://play.google.com/store/apps/details?id=com.hohoedu.app";
            String iosUrl = "https://apps.apple.com/us/app/id6504266908";

            // 알림톡 내용
            String content = template.getTemplate()
                    .replace("#{name}", centerName)
                    .replace("#{join}", joinUrl)
                    .replace("#{Android}", androidUrl)
                    .replace("#{iOS}", iosUrl);


            // 다른 sendATS 메서드 시도
            String receiptNum = kakaoService.sendATS(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode(),
                    config.getSenderNumber(),
                    content,
                    (String) null,
                    null,
                    phone,
                    "신규회원",
                    (String) null
            );

            KakaoSentInfo sentInfo = kakaoService.getMessages(
                    config.getCorpNumber(),
                    receiptNum
            );

            log.info("=== 전송 상세 정보 ===");
            log.info("접수번호: {}", sentInfo.getSendNum());
            log.info("성공건수: {}", sentInfo.getSuccessCnt());
            log.info("실패건수: {}", sentInfo.getFailCnt());
            log.info("취소건수: {}", sentInfo.getCancelCnt());

            PopbillReqDTO.PopbillSendLogReqDTO sendLogDTO = PopbillReqDTO.PopbillSendLogReqDTO.builder()
                    .sendKey(sendKey)
                    .userCode(userCode)
                    .receiverPhone(phone)
                    .centerCode(centerCode)
                    .sendType("INVITE_LINK")
                    .templateCode(respDTO.getPopbillTemplateCode())
                    .content(content)
                    .sendStatus("SUCCESS")
                    .build();

            popbillRepository.insertSendLog(sendLogDTO);

            PopbillReqDTO.InviteTrackingReqDTO inviteDTO = PopbillReqDTO.InviteTrackingReqDTO.builder()
                    .sendKey(sendKey)
                    .inviteCode(inviteCode)
                    .userCode(userCode)
                    .receiverPhone(phone)
                    .centerCode(centerCode)
                    .build();


            popbillRepository.insertInviteTracking(inviteDTO);

            return receiptNum;


        } catch (PopbillException e) {
            log.error("팝빌 알림톡 전송 실패 - code: {}, message: {}", e.getCode(), e.getMessage());
            throw new RuntimeException("알림톡 전송 실패: " + e.getMessage(), e);
        }
    }

    public String sendJoinCompletionAlimtalk(String centerCode, String teacherPhone, String parentPhone, String userCode, StudentWebReqDTO.StudentJoinDTO studentDTO) {
        String category = "join_complete";
        try {

            KakaoService kakaoService = serviceFactory.getKakaoService(centerCode);

            PopbillConfig config = popbillRepository.findPopbillConfig(centerCode);
            PopbillRespDTO.PopbillTemplateRespDTO respDTO = popbillRepository.findPopbillTemplate(centerCode, category);
            ATSTemplate template = kakaoService.getATSTemplate(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode()
            );

            String grade = studentRepository.findByGradeKey(studentDTO.getGradeKey());

            // 알림톡 내용
            String content = template.getTemplate()
                    .replace("#{학생명}", studentDTO.getStudentName())
                    .replace("#{선택과목}", "미지정")
                    .replace("#{연령/학년}", grade != null ? grade : "미지정")
                    .replace("#{연락처}", parentPhone);
            log.info(content);
            log.info(teacherPhone);
            // 알림톡 전송
            String receiptNum = kakaoService.sendATS(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode(),
                    config.getSenderNumber(),
                    content,
                    (String) null,
                    null,
                    teacherPhone,  // 선생님 전화번호
                    "선생님",
                    (String) null
            );

            KakaoSentInfo sentInfo = kakaoService.getMessages(
                    config.getCorpNumber(),
                    receiptNum
            );

            log.info("=== 가입 완료 알림 전송 정보 ===");
            log.info("접수번호: {}", sentInfo.getSendNum());
            log.info("성공건수: {}", sentInfo.getSuccessCnt());
            log.info("실패건수: {}", sentInfo.getFailCnt());

            // 전송 로그 저장
            String sendKey = KeyGenerator.generateSendKey();
            PopbillReqDTO.PopbillSendLogReqDTO sendLogDTO = PopbillReqDTO.PopbillSendLogReqDTO.builder()
                    .sendKey(sendKey)
                    .userCode(userCode)
                    .receiverPhone(teacherPhone)
                    .centerCode(centerCode)
                    .sendType("JOIN_COMPLETION")  // 타입 변경
                    .templateCode(respDTO.getPopbillTemplateCode())
                    .content(content)
                    .sendStatus("SUCCESS")
                    .build();

            popbillRepository.insertSendLog(sendLogDTO);

            return receiptNum;

        } catch (PopbillException e) {
            log.error("가입 완료 알림톡 전송 실패 - code: {}, message: {}", e.getCode(), e.getMessage());
            throw new RuntimeException("가입 완료 알림톡 전송 실패: " + e.getMessage(), e);
        }
    }

    public String sendAddReorderAlimtalk(UserRespDTO.LoginRespDTO user, String orderContent) {
        String category = "add_reorder";
        try {
            // 1. KakaoService 가져오기
            KakaoService kakaoService = serviceFactory.getKakaoService("PUS001");

            // 2. Config 및 Template 조회
            PopbillConfig config = popbillRepository.findPopbillConfig("PUS001");
            PopbillRespDTO.PopbillTemplateRespDTO respDTO = popbillRepository.findPopbillTemplate("PUS001", category);

            // 3. 템플릿 가져오기
            ATSTemplate template = kakaoService.getATSTemplate(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode()
            );

            log.info("=== 추가 주문 알림톡 템플릿 정보 ===");
            log.info("템플릿명: {}", template.getTemplateName());
            log.info("템플릿 내용: {}", template.getTemplate());

            // 4. 템플릿 내용 치환
            String content = template.getTemplate()
                    .replace("#{지점명}", user.getCenterName()+" 선생님")
                    .replace("#{선생님명}", user.getUserName())
                    .replace("#{수정횟수}", "1")
                    .replace("#{수정시간}", "2026-02-04 22:09")
                    .replace("#{주문내용}", orderContent);

            log.info("=== 전송할 내용 ===");
            log.info(content);

            // 5. 알림톡 전송 (본사 번호로)
            String receiptNum = kakaoService.sendATS(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode(),
                    config.getSenderNumber(),
                    content,
                    (String) null,
                    null,
                    "01062954886",  // 본사 전화번호
                    "본사",
                    (String) null
            );

            // 6. 전송 결과 확인
            KakaoSentInfo sentInfo = kakaoService.getMessages(
                    config.getCorpNumber(),
                    receiptNum
            );

            log.info("=== 추가 주문 알림 전송 정보 ===");
            log.info("접수번호: {}", sentInfo.getSendNum());
            log.info("성공건수: {}", sentInfo.getSuccessCnt());
            log.info("실패건수: {}", sentInfo.getFailCnt());

            // 7. 전송 로그 저장
            String sendKey = KeyGenerator.generateSendKey();
            PopbillReqDTO.PopbillSendLogReqDTO sendLogDTO = PopbillReqDTO.PopbillSendLogReqDTO.builder()
                    .sendKey(sendKey)
                    .userCode(user.getUserCode())
                    .receiverPhone("01062954886")
                    .centerCode(user.getCenterCode())
                    .sendType("ADD_REORDER")
                    .templateCode(respDTO.getPopbillTemplateCode())
                    .content(content)
                    .sendStatus("SUCCESS")
                    .build();

            popbillRepository.insertSendLog(sendLogDTO);

            return receiptNum;

        } catch (PopbillException e) {
            log.error("추가 주문 알림톡 전송 실패 - code: {}, message: {}", e.getCode(), e.getMessage());
            throw new RuntimeException("추가 주문 알림톡 전송 실패: " + e.getMessage(), e);
        }
    }
}



