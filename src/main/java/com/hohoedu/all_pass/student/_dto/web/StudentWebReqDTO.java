package com.hohoedu.all_pass.student._dto.web;

import java.util.List;

import lombok.Data;

public class StudentWebReqDTO {

    @Data
    public static class StudentJoinDTO {
        private Integer id;
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
        private String appId;
        private String appPassword;
        private String centerCode;
        private String statusKey;
        private String gradeKey;
        private String levelKey;
    }

    @Data
    public static class ParentJoinDTO {
        private Integer id;
        private String parentName;
        private String parentTelFirst;
        private String parentTelMiddle;
        private String parentTelLast;
        private String relationKey;
        private boolean parentPrivacyAgree;
        private String studentId;
    }

    @Data
    public static class StatusHistoryDTO {
        private Integer id;
        private String statusKey;
        private String studentId;
        private String reason;
        private String userCode;
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
    public static class StudentClassSaveReqDTO {
        private String studentId;

        // 한자 수강 정보
        private String hanClassKey;
        private String hanTeacherCode;
        private String hanStatus;
        private String hanEntryDate;
        private Integer hanFee;
        private Integer hanMaterialFee;

        // 독서 수강 정보
        private String bookClassKey;
        private String bookTeacherCode;
        private String bookStatus;
        private String bookEntryDate;
        private Integer bookFee;
        private Integer bookMaterialFee;
    }

    @Data
    public static class StudentTransferDTO {
        private List<String> students;
        private String selectedHan;
        private String selectedBook;
        private String userCode;
        private String moveAt;
        private String transferReason;

    }
}