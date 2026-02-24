package com.hohoedu.all_pass.popbill._dto;

import lombok.*;

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

    @Data
    @Builder
    public static class PopbillSendLogReqDTO {
        private String sendKey;
        private String userCode;
        private String centerCode;
        private String receiverPhone;
        private String sendType;
        private String templateCode;
        private String content;
        private String sendStatus;
    }

    @Data
    @Builder
    public static class InviteTrackingReqDTO {
        private String sendKey;
        private String inviteCode;
        private String userCode;
        private String receiverPhone;
        private String centerCode;
        private String inviteStatus;
    }

    @Data
    @Builder
    public static class RemindReqDTO {
        private String year;
        private String month;
        private List<RemindStudentDTO> students;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RemindStudentDTO {
            private String studentName;
            private String parentPhone;
            private Long totalUnpaidAmount;
            private String paymentKey;
        }
    }
}
