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
        private String consultType;
    }

    @Data
    public static class GetConsultReqDTO {
        private String userCode;
        private String startDate;
        private String endDate;

    }


    @Data
    public static class ConsultUpdateContentDTO {
        private Integer consultId;
        private String consultDate;
        private String content;
    }
}
