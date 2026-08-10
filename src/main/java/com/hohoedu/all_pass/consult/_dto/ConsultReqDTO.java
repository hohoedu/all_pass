package com.hohoedu.all_pass.consult._dto;

import lombok.Data;

public class ConsultReqDTO {

    @Data
    public static class ConsultRegisterReqDTO {
        private Integer consultId;
        private String studentName;
        private String consultDate;
        private String school;
        private String phone;
        private String gradeKey;
        private String subject;
        private String inflowRouteKey;
        private String progressKey;
        private String consultKey;
        private String content;
        private String centerCode;
        private String userCode;
        private String consultType;

        private boolean inquiryHoho;
        private boolean inquiryHan;
        private boolean inquiryBook;
        private boolean inquiryDoc;
    }

    @Data
    public static class ConsultListReqDTO {
        private String centerCode;
        private String userCode;
        private String startDate;
        private String endDate;
        private String progress;
        private String keyword;
        private String gradeKey;
        private String subject;
        private String sortColumn;
        private String sortDir;
    }

    @Data
    public static class ConsultModalReqDTO {
        private String keyword;
        private String centerCode;
    }

    @Data
    public static class ConsultMemoUpdateReqDTO {
        private Integer id;
        private String content;
    }

    @Data
    public static class ConsultUpdateProgressReqDTO {
        private Integer id;
        private String progressKey;
        private String endReason;
        private String userCode;
    }

    @Data
    public static class ConsultPrintReqDTO {
        private String startDate;
        private String endDate;
        private String progress;
        private String keyword;
        private String gradeKey;
        private String subject;
        private String userCode;
        private String sortColumn;
        private String sortDir;
        private String centerCode;
    }
}
