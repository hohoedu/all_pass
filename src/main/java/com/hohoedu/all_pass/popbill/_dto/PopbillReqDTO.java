package com.hohoedu.all_pass.popbill._dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class PopbillReqDTO {
    @Data
    public static class PopbillInsertReqDTO {
        private String corpNumber;
        private String linkId;
        private String popbillId;
        private String secretKey;
        private String senderNumber;
        private String centerCode;
    }

    @Data
    public static class PopbillSendRequest {

        private String centerCode;
        private String templateCode;
        private String receiverNum;
        private String receiverName;
        private String content;
        private String altSendType;
        private String altContent;
        private String altSendDate;
        private String sendDate;
        private List<ButtonInfo> buttons;

        @Getter
        @Setter
        public static class ButtonInfo {
            private String n;    // 버튼명
            private String t;    // 버튼타입 (WL: 웹링크, AL: 앱링크)
            private String u1;   // 링크1 (모바일)
            private String u2;   // 링크2 (PC)
        }
    }
}
