package com.hohoedu.all_pass.consult._dto;

import lombok.Data;

public class ConsultRespDTO {
    @Data
    public static class ConsultDTO {
        private Integer id;
        private String consultKey;
        private String studentName;
        private String consultDate;
        private String school;
        private String phone;
        private String gradeKey;
        private String gradeName;
        private String inflowRouteName;
        private String progressKey;
        private String content;
        private String sendAt;
        private String type;
        private String registerDate;
        private String inflowRouteKey;
        private String userName;
        private String registeredAt;
        private String endReason;
        private String endedAt;
        private String endUserName;
        private boolean inquiryHoho;
        private boolean inquiryHan;
        private boolean inquiryBook;
        private boolean inquiryDoc;
    }

    @Data
    public static class ConsultPrintDTO {
        private Integer id;
        private String consultKey;
        private String username;
        private String consultDate;
        private String studentName;
        private String school;
        private String gradeName;
        private String phone;
        private String content;
        private String inflowRouteName;
        private String progressKey;
        private String progressName;
        private String sendAt;
        private String type;
    }

    @Data
    public static class ConsultModalRespDTO {
        private Integer id;
        private String studentName;
        private String phone;
        private String school;
        private String gradeKey;
        private String gradeName;
        private String inflowRouteKey;
        private String type;
        private String content;
        private String lastConsultDate;
        private String progressKey;
        private int consultCount;
    }

}
