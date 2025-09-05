package com.hohoedu.all_pass.consult._dto;

import lombok.Data;

public class ConsultRespDTO {
    @Data
    public static class ConsultDTO {
        private Integer consultNo;
        private String studentName;
        private String consultDate;
        private String school;
        private String phone;
        private String grade;
        private String inflowRoute;
        private String content;
    }
}
