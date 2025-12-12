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

    public String sendJoinAlimtalk(String centerCode, String phone, String regionName, String centerName) {

        try {

            KakaoService kakaoService = serviceFactory.getKakaoService(centerCode);

            PopbillConfig config = popbillRepository.findPopbillConfig(centerCode);

            // 알림톡 내용
            String content = String.format(
                    "[호호서당]\n%s %s에 오신 것을 환영합니다!\n\n" +
                            "회원가입을 진행하시려면 아래 링크를 클릭해주세요.\n\n" +
                            "링크: https://hohoedu.com/join?center=%s",
                    regionName,
                    centerName,
                    centerCode
            );

            // 대체문자 내용
            String altContent = String.format(
                    "[호호서당] %s %s 회원가입 안내\nhttps://hohoedu.com/join?center=%s",
                    regionName,
                    centerName,
                    centerCode
            );

            // 🔥 다른 sendATS 메서드 시도
            String receiptNum = kakaoService.sendATS(
                    config.getCorpNumber(),      // 사업자번호
                    "025120000348",           // 템플릿 코드
                    config.getSenderNumber(), // 발신번호
                    phone,                    // 수신번호
                    "신규회원",                // 수신자명
                    content,                  // 알림톡 내용
                    altContent,               // 대체문자 내용
                    "A",                      // 대체문자 발송타입
                    (String) null,                     // 예약일시
                    (String) null                      // 버튼
            );

            log.info("알림톡 전송 성공 - receiptNum: {}, receiver: {}", receiptNum, phone);
            return receiptNum;

        } catch (PopbillException e) {
            log.error("팝빌 알림톡 전송 실패 - code: {}, message: {}", e.getCode(), e.getMessage());
            throw new RuntimeException("알림톡 전송 실패: " + e.getMessage(), e);
        }
    }
}



