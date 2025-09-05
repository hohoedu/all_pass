package com.hohoedu.all_pass.student._dto;

import java.util.List;

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
    public static class StatusHistoryDTO {
        private Integer historyNo;
        private String statusNo;
        private Integer studentNo;
        private String reason;
    }

    @Data
    public static class StudentUpdateDTO {
        private String studentName;
        private String statusNo;
        private String birth;
        private String gender;
        private String school;
        private String address;
        private String addressDetail;
        private String grade;
    }

    @Data
    public static class StudentTransferDTO {
        private Integer studentNo;
        private String inoutHan;
        private String inoutRead;
        private List<Integer> studentNoList;
        private String userCode;
        private String moveAt;
        private String transferReason;
    }

}