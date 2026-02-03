package com.hohoedu.all_pass.popbill;

import com.hohoedu.all_pass._core.utils.Aes256Util;
import com.hohoedu.all_pass._core.utils.PopbillServiceFactory;
import com.hohoedu.all_pass._core.utils.KeyGenerator;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.popbill._dto.PopbillRespDTO;
import com.hohoedu.all_pass.popbill.repository.PopbillRepository;
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
            PopbillRespDTO.PopbillTemplateRespDTO respDTO = popbillRepository.findPopbillTemplate(centerCode);
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
//            String joinUrl = "http://192.168.0.8:8080/student/mobile/join?centerCode=" + centerCode + "&invite=" + inviteCode;
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

    public String sendJoinCompletionAlimtalk(String centerCode, String teacherPhone, String parentPhone, String userCode) {

        try {

            KakaoService kakaoService = serviceFactory.getKakaoService(centerCode);

            PopbillConfig config = popbillRepository.findPopbillConfig(centerCode);
            PopbillRespDTO.PopbillTemplateRespDTO respDTO = popbillRepository.findPopbillTemplate(centerCode);
            ATSTemplate template = kakaoService.getATSTemplate(
                    config.getCorpNumber(),
                    respDTO.getPopbillTemplateCode()
            );

            // 알림톡 내용
            String content = template.getTemplate();

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
}



