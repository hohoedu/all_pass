package com.hohoedu.all_pass.student._dto.web;

import java.sql.Timestamp;
import java.util.List;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.app.StudentAppReqDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class StudentWebRespDTO {

    @Data
    public static class StudentsListDTO {
        private String studentId;
        private String studentName;
        private String school;
        private String gradeName;
        private String levelName;
        private String gender;
        private String birth;
        private String address;
        private String addressDetail;
        private String phone;
        private String center;
        private String status;
        private String statusName;
        private String reason;
        private String hanClass;
        private String bookClass;
        private Timestamp createdAt;
        private String isSibling;
        private String classType;
        private List<SiblingInfoDTO> siblingList;

        @Data
        public static class SiblingInfoDTO {
            private String siblingName;
            private String grade;
        }
    }

    @Data
    @Builder
    public static class StudentDTO {
        private StudentInfoDTO studentInfo;
        private StudentPaymentDTO studentPayment;
        private List<StudentAttendanceDTO> studentAttendance;
        private List<StudentConsultDTO> studentCounsult;
        private List<GradeCode> gradeCodes;
    }

    @Data
    public static class StudentPaymentDTO {
        private String hanState;
        private String bookState;
        private String hanMaterialPrice;
        private String bookMaterialPrice;
        private String hanClassKey;
        private String bookClassKey;
        private String hanTeacher;
        private String bookTeacher;
        private String hanClassName;
        private String bookClassName;
        private String hanFee;
        private String bookFee;
        private String entryHanDate;
        private String entryBookDate;
    }


    @Data
    public static class StudentAttendanceDTO {

    }

    @Data
    public static class StudentConsultDTO {

    }

    @Data
    public static class StudentInfoDTO {
        private String studentId;
        private String studentName;
        private String statusKey;
        private String statusName;
        private String hanClass;
        private String bookClass;
        private String parentPhone;
        private String parentRelation;
        private String school;
        private String gradeKey;
        private String gradeName;
        private String birth;
        private String genderKey;
        private String address;
        private String addressDetail;
    }

    @Data
    public static class StudentFilterDTO {
        private Integer studentNo;
        private String studentName;
        private String school;
        private String grade;
        private String centerNo;
        private String status;
        private String hanClass;
        private String bookClass;
        private String isSibling;
    }

    @Data
    public static class StudentStatusDTO {
        private String studentId;
        private String statusKey;
        private String statusName;
    }

    @Data
    public static class MainStudentDTO {
        private String studentId;
        private String studentName;
        private String statusName;
        private String gradeName;
        private String school;
        private String centerName;
        private String entryDate;
        private String hanClass;
        private String bookClass;
        private String hasApp;
        private String appId;

    }


    @Data
    public static class StudentInOutDTO {
        private String studentId;
        private String studentName;
        private String hanClass;
        private String bookClass;
        private String gradeName;
        private String moveAt;
        private String phone;
        private String transferReson;
        private String hanTeacher;
        private String bookTeacher;
        private String entryDate;
        private String hanClassType;
        private String bookClassType;
    }

    @Data
    public static class StudentTransferDTO {
        private Integer historyNo;
        private String moveAt;
        private String studentName;
        private String className;
        private String fromTeacher;
        private String toTeacher;
        private String transferReason;
    }

    @Data
    public static class TransferTimeTableInfoDTO {
        private String userCode;
        private String timeTableKey;
        private String classKey;
    }

    @Data
    public static class TeacherDTO {
        private Student studentId;
        private Boolean hanState;
        private Boolean bookState;
        private String entryHanDate;
        private String entryBookDate;
        private String assignHanTeacher;
        private String assignHanClass;
        private String assignBookTeacher;
        private String assignBookClass;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentSnapshotRespDTO {
        private String snapshotYm;  // 집계 년월
        private String centerCode;
        private int totalCount;     // 총원
        private int activeCount;    // 재원
        private int restCount;      // 휴원
        private int withdrawnCount; // 탈퇴
        private int waitCount;      // 대기
    }

    @Data
    public static class StudentOverviewDTO {
        private String ym;
        private String classKey;
        private String entryCount;
        private String withdrawCount;
        private String moveInCount;
        private String moveOutCount;

    }

    @Data
    public static class WithdrawCountDTO {
        private int joinCount;
        private int withdrawCount;
        private int transferInCount;
        private int transferOutCount;
        private int graduateCount;
    }

    @Data
    public static class WithdrawItemDTO {
        private String studentId;
        private String studentName;
        private String className;
        private String teacherName;
        private String transferInTeacher;
        private String transferOutTeacher;
        private String gradeName;
        private String joinDate;
        private String withdrawDate;
        private String transferDate;
        private String graduateDate;
        private String reason;
    }

}
