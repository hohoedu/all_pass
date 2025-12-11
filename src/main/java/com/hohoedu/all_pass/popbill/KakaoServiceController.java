package com.hohoedu.all_pass.popbill;

import com.popbill.api.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.PopbillException;
import com.popbill.api.kakao.KakaoButton;

@Controller
@RequiredArgsConstructor
public class KakaoServiceController {

    private final KakaoService kakaoService;

    @RequestMapping(value = "sendATS_one", method = RequestMethod.GET)
    public String sendATS_one(Model m) {

        String CorpNum = "3208800028";

        // 승인된 알림톡 템플릿코드
        // └ 알림톡 템플릿 관리 팝업 URL(GetATSTemplateMgtURL API) 함수, 알림톡 템플릿 목록
        // 확인(ListATStemplate API) 함수를 호출하거나
        // 팝빌사이트에서 승인된 알림톡 템플릿 코드를 확인 가능.
        String templateCode = "022070000338";

        // 발신번호
        // altSendType = 'C' / 'A' 일 경우, 대체문자를 전송할 발신번호
        // altSendType = '' 일 경우, null 또는 공백 처리
        // ※ 대체문자를 전송하는 경우에는 사전에 등록된 발신번호 입력 필수
        String senderNum = "18990898";

        // 알림톡 내용 (최대 1000자)
        String content = "[ 팝빌 ]\n"
                + "신청하신 #{템플릿코드}에 대한 심사가 완료되어 승인 처리되었습니다.\n"
                + "해당 템플릿으로 전송 가능합니다.\n\n"
                + "문의사항 있으시면 파트너센터로 편하게 연락주시기 바랍니다.\n\n"
                + "팝빌 파트너센터 : 1600-8536\n"
                + "support@linkhub.co.kr";

        // 대체문자 제목
        // - 메시지 길이(90byte)에 따라 장문(LMS)인 경우에만 적용.
        String altSubject = "대체문자 제목1234";

        // 대체문자 내용, 대체문자 유형(altSendType)이 "A"일 경우, 대체문자로 전송할 내용 (최대 2000byte)
        // └ 팝빌이 메시지 길이에 따라 단문(90byte 이하) 또는 장문(90byte 초과)으로 전송처리
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = 대체문자 내용(altContent)에 입력한 내용 전송
        String altSendType = "C";

        // 수신번호
        String receiverNum = "01062954886";

        // 수신자명
        String receiverName = "박세환 테스트";

        // 전송 예약일시, 형태(yyyyMMddHHmmss)
        // - 분단위 전송, 미입력 시 즉시 전송
        String sndDT = "";

        // 팝빌회원 아이디
        String UserID = "stst8898";

        // 요청번호
        // 팝빌이 접수 단위를 식별할 수 있도록 파트너가 할당한 식별번호.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        // 버튼 목록, 알림톡 버튼정보를 템플릿 신청시 기재한 버튼정보와 동일하게 전송하는 경우 null 처리.
        KakaoButton[] btns = null;

        // 버튼 목록, 알림톡 버튼 URL에 #{템플릿변수}를 기재한경우 템플릿변수 영역을 변경하여 버튼정보 구성
        // KakaoButton[] btns = new KakaoButton[1];

        // KakaoButton button = new KakaoButton();
        // button.setN("버튼명"); // 버튼명
        // button.setT("WL"); // 버튼 유형
        // button.setU1("https://www.popbill.com"); // 버튼링크
        // button.setU2("http://test.popbill.com"); // 버튼링크
        // button.setTg("out"); // 아웃 링크
        // btns[0] = button;

        try {

            String receiptNum = kakaoService.sendATS(CorpNum, templateCode, senderNum,
                    content, altSubject, altContent, altSendType, receiverNum, receiverName,
                    sndDT, UserID, requestNum, btns);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            // 예외 발생 시, e.getCode() 로 오류 코드를 확인하고, e.getMessage()로 오류 메시지를 확인합니다.
            System.out.println("오류 코드" + e.getCode());
            System.out.println("오류 메시지" + e.getMessage());
        }

        return "response";
    }
}
