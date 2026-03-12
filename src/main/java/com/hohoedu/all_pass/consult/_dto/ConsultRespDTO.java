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
    }
}
