package com.hohoedu.all_pass.popbill;

import com.hohoedu.all_pass._core.utils.Aes256Util;
import com.hohoedu.all_pass._core.utils.PopbillServiceFactory;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.popbill.repository.PopbillRepository;
import com.popbill.api.KakaoService;
import com.popbill.api.PopbillException;
import com.popbill.api.kakao.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

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

    public String sendJoinAlimtalk(String centerCode, String phone, String regionName, String centerName) {

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

            ATSTemplate template = kakaoService.getATSTemplate(
                    config.getCorpNumber(),
                    "025120000348"
            );

            log.info("=== 템플릿 정보 ===");
            log.info("템플릿명: {}", template.getTemplateName());
            log.info("템플릿 내용: {}", template.getTemplate());
            log.info("승인상태: {}", template.getState()); // 중요!
            log.info("등록일시: {}", template.getStateDT());

            String joinUrl = "https://hohocenter.co.kr/student/mobile/join?centerCode=" + centerCode;
            String androidUrl = "https://play.google.com/store/apps/details?id=com.hohoedu.app";
            String iosUrl = "https://apps.apple.com/us/app/id6504266908";

            // 알림톡 내용
            String content = String.format(
                    "안녕하세요, 학부모님.\n" +
                            "해와나무교육 입회를 환영합니다.\n\n" +
                            "입회 등록 신청서 작성은 아이의 수업 등록, 출석 관리, 학습 기록 및 학부모 앱 연동을 위한 필수 절차입니다.\n" +
                            "원활한 이용을 위해 반드시 등록을 부탁드립니다.\n\n" +
                            "아래 링크를 통해\n" +
                            "1) 입회 등록 신청서 작성\n" +
                            "2) i-위드 앱(학부모 전용) 설치\n" +
                            "를 진행해 주시기 바랍니다.\n\n" +
                            "▶ 입회 등록 신청서 바로가기\n" +
                            "%s\n\n" +
                            "▶  i-위드 앱 설치(Android)\n" +
                            "%s\n\n" +
                            "▶  i-위드 앱 설치(iOS)\n" +
                            "%s\n\n" +
                            "입회 등록 및 앱 설치가 완료되면 수업 안내, 학습 내용, 출석 및 공지사항을 앱을 통해 확인하실 수 있습니다.\n\n" +
                            "진행 중 궁금하신 점이 있으시면 언제든지 학원으로 문의해 주세요.\n" +
                            "감사합니다.",
                    joinUrl,     // #{join}
                    androidUrl,  // #{Android}
                    iosUrl       // #{iOS}
            );

            // 🔥 다른 sendATS 메서드 시도
            String receiptNum = kakaoService.sendATS(
                    config.getCorpNumber(),   // 사업자번호
                    "025120000348",           // 템플릿 코드
                    config.getSenderNumber(), // 발신번호
                    content,                  // 알림톡 내용
                    (String) null,            // 대체문자 내용
                    null,                     // 대체문자 발송타입
                    phone,                    // 수신번호
                    "신규회원",                 // 수신자명
                    (String) null             // 예약일시
            );

            log.info("알림톡 전송 성공 - receiptNum: {}, receiver: {}", receiptNum, phone);

            KakaoSentInfo sentInfo = kakaoService.getMessages(
                    config.getCorpNumber(),
                    receiptNum
            );

            log.info("=== 전송 상세 정보 ===");
            log.info("접수번호: {}", sentInfo.getSendNum());
            log.info("성공건수: {}", sentInfo.getSuccessCnt());
            log.info("실패건수: {}", sentInfo.getFailCnt());
            log.info("취소건수: {}", sentInfo.getCancelCnt());

            return receiptNum;


        } catch (PopbillException e) {
            log.error("팝빌 알림톡 전송 실패 - code: {}, message: {}", e.getCode(), e.getMessage());
            throw new RuntimeException("알림톡 전송 실패: " + e.getMessage(), e);
        }
    }
}



