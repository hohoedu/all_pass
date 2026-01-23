package com.hohoedu.all_pass.consult._dto;

import lombok.Data;

public class ConsultReqDTO {

    @Data
    public static class ConsultRegisterReqDTO {
        private Integer id;
        private String studentName;
        private String consultDate;
        private String school;
        private String phone;
        private String gradeKey;
        private String inflowRouteKey;
        private String content;
        private String centerCode;
        private String userCode;
    }

    @Data
    public static class GetConsultReqDTO {
        private String userCode;
        private String startYm;
        private String endYm;

    }
    
}
