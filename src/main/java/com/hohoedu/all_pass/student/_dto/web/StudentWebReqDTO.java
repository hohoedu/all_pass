package com.hohoedu.all_pass.student._dto.web;

import java.util.List;

import lombok.*;

public class StudentWebReqDTO {

    @Data
    public static class StudentJoinDTO {
        private Integer id;
        private String studentId;
        private String studentName;
        private String birth;
        private String inviteCode;
        private boolean gender;
        private String school;
        private String address;
        private String addressDetail;
        private String entryHanDate;
        private String entryBookDate;
        private boolean studentPrivacyAgree;
        private String appId;
        private String appPassword;
        private String userCode;
        private String centerCode;
        private String statusKey;
        private String gradeKey;
        private String gradeName;
        private String levelKey;
        private String subject;
        private boolean subHoho;
        private boolean subHan;
        private boolean subBook;
        private String billingPhone;
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
        private String signature;
    }

    @Data
    public static class StatusHistoryDTO {
        private Integer id;
        private String statusKey;
        private String studentId;
        private String reason;
        private String userCode;
        private String withdrawDate;
    }

    @Data
    public static class StudentUpdateDTO {

        private String studentId;
        private String studentName;
        private String birth;
        private String school;
        private String address;
        private String addressDetail;
        private String parentPhone;
        private String gradeKey;
        private String genderKey;
        private String relationKey;
        private String billingPhone;

        private String appId;

        private boolean cashTypePersonal;
        private boolean cashTypeCorporate;
        private String cashReceiptNumber;

        private String siblingSearchType;
        private String siblingSearchValue;
        private String siblingSavePhone;

        private String entryHanDate;
        private String entryBookDate;

        private String userCode;
    }

    @Data
    public static class StudentPaymentUpdateDTO {
        private String studentId;
        private String entryHanDate;
        private String entryBookDate;
        private Integer hanMaterialFee;
        private Integer bookMaterialFee;
        private Integer hanEduFee;
        private Integer bookEduFee;
        private String paymentKey;
    }

    @Data
    public static class StudentCourseUpdateDTO {

        private String studentId;

        private Integer hanState;
        private Integer bookState;

        private Boolean hanChanged;
        private Boolean bookChanged;

        private String entryHanDate;
        private String entryBookDate;

        private String inactiveHanDate;
        private String inactiveBookDate;

        private String inactiveHanReason;
        private String inactiveBookReason;

        private String hanClassKey;
        private String bookClassKey;
        private String hanTeacherCode;
        private String bookTeacherCode;
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

    @Data
    public static class TransferCancelDTO {
        private List<Integer> ids;
    }

    @Data
    public static class StudentAttendanceUpdateDTO {

        private String attendanceKey;
        private String studentId;
        private String timeTableKey;
        private String absenceDate;
        private String week;
        private String yy;
        private String mm;
        private String centerCode;
        private String attendanceName;
        private String inTime;
        private String outTime;

    }

    @Data
    public static class TransferStudentListReqDTO {
        private String userCode;
        private String centerCode;
    }

    @Data
    public static class WithdrawReqDTO {
        private String userCode;
        private String ym;
        private String roleKey;
        private String centerCode;
    }

    @Data
    public static class UpdateAppIdLogDTO {
        private String studentId;
        private String currentAppId;
        private String prevAppId;
        private String userCode;
        private boolean isManual;
    }

    @Data
    public static class FamilyLinkRequest {
        private String studentId; // 현재 학생
        private String siblingId; // 연결할 형제 학생
    }

    @Data
    public static class StudentRestoreDTO {
        private String studentId;
    }

    @Data
    public static class QrRegisterDTO {
        private String studentId;
        private String qrNumber;
    }

    @Data
    public static class QrUpdateDTO {
        private String studentId;
        private String qrNumber;
    }

    @Data
    public static class PrivacySearchReqDTO {
        private String yy;
        private String mm;
        private String search;
    }

    @Data
    public static class PrivacyPrintReqDTO {
        private List<String> studentIds;
    }
}