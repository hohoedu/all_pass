package com.hohoedu.all_pass.consult._dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class ConsultReqDTO {

    @Data
    public static class ConsultRegisterReqDTO {
        private Integer consultId;
        private String studentName;
        private String consultDate;
        private String school;
        private String phone;
        private String gradeKey;
        private String inflowRouteKey;
        private String progressKey;
        private String content;
        private String centerCode;
        private String userCode;
        private String consultType;
    }

    @Data
    public static class ConsultListReqDTO {
        private String centerCode;
        private String startDate;
        private String endDate;
        private String progress;
        private String keyword;
        private String sortColumn;
        private String sortDir;
    }

    @Data
    public static class ConsultModalReqDTO {
        private String keyword;
        private String centerCode;
    }


    @Data
    public static class ConsultUpdateContentDTO {
        private Integer consultId;
        private String consultDate;
        private String content;
    }

    @Data
    public static class ConsultUpdateProgressReqDTO {
        private Integer id;
        private String progressKey;
        private String endReason;
    }

    @Data
    public static class ConsultPrintReqDTO {
        private String startDate;
        private String endDate;
        private String progress;
        private String keyword;
        private String sortColumn;
        private String sortDir;
        private String centerCode;
    }
}
