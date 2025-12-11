package com.hohoedu.all_pass.popbill;

import com.popbill.api.KakaoService;
import com.popbill.api.PopbillException;
import com.popbill.api.kakao.KakaoReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class PopbillService {

    private final KakaoService kakaoService;
    private final PopbillProperties popbillProperties;

    public String sendAts(
            String centerCode,
            String templateCode,
            String receiver,
            String receiverName,
            String content
    ) throws PopbillException {

        PopbillCenterProperties center = popbillProperties.getCenters().get(centerCode);

        if (center == null) {
            throw new IllegalArgumentException("잘못된 센터 코드: " + centerCode);
        }

        String corpNum = center.getCorpNum();
        String sender = center.getSenderNumber();
        String userId = center.getUserId();

        // KakaoReceiver 배열 생성 (단건)
        KakaoReceiver rc = new KakaoReceiver();
        rc.setReceiverNum(receiver);      // 수신번호
        rc.setReceiverName(receiverName);    // 수신자명
        rc.setMessage(content);              // 알림톡 본문 (중요)

        KakaoReceiver[] receivers = new KakaoReceiver[] { rc };

        // altContent = null (대체문자 없음)
        String altContent = null;

        // sendATS 호출
        return kakaoService.sendATS(
                corpNum,
                templateCode,
                sender,
                null,
                altContent,
                "C",
                rc.getReceiverNum(),
                rc.getReceiverName(),
                "20251211210000"
        );
    }

}