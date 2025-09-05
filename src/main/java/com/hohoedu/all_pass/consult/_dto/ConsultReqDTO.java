package com.hohoedu.all_pass.consult._dto;

import lombok.Data;

public class ConsultReqDTO {

    @Data
    public static class ConsultRegisterReqDTO {
        private Integer consultNo;
        private String studentName;
        private String consultDate;
        private String school;
        private String phone;
        private Integer gradeNo;
        private Integer inflowRouteNo;
        private String content;
    }
    
}
