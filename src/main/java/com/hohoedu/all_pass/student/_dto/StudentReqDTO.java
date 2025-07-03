package com.hohoedu.all_pass.student._dto;

import lombok.Data;

public class StudentReqDTO {

    @Data
    public static class StudentJoinDTO {
        private Integer studentNo;
        private String studentId;
        private String studentName;
        private String birth;
        private boolean gender;
        private String school;
        private String address;
        private String addressDetail;
        private String entryHanDate;
        private String entryBookDate;
        private boolean studentPrivacyAgree;
        private String centerNo;
        private Integer statusNo;
        private Integer gradeNo;
        private Integer levelNo;

    }

    @Data
    public static class ParentJoinDTO {
        private Integer parentNo;
        private String parentName;
        private String parentTelFirst;
        private String parentTelMiddle;
        private String parentTelLast;
        private Integer relationNo;
        private boolean parentPrivacyAgree;
        private Integer studentNo;
    }

    @Data
    public static class StatusHistoryDTO{
        private Integer historyNo;
        private String statusNo;
        private String studentNo;
        private String reason;
    }
}