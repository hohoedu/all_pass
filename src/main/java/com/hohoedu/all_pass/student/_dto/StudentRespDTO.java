package com.hohoedu.all_pass.student._dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class StudentRespDTO {

    @Data
    public static class StudentsListDTO {
        private Integer studentNo;
        private String school;
        private String studentName;
        private String grade;
        private String level;
        private String gender;
        private String birth;
        private String address;
        private String addressDetail;
        private String center;
        private String entryHanDate;
        private String entryBookDate;
        private Timestamp statusModifiedAt;
        private String status;
        private String reason;
        private String hanClass;
        private String bookClass;
        private Timestamp createdAt;
        private String isSibling;
        private List<SiblingInfoDTO> siblingList;

        @Data
        public static class SiblingInfoDTO {
            private String siblingName;
            private String grade;
        }
    }

    @Data
    public static class StudentDTO {
        private Integer studentNo;
        private String school;
        private String studentName;
        private String grade;
        private String level;
        private String gender;
        private String birth;
        private String address;
        private String addressDetail;
        private String center;
        private String entryHanDate;
        private String entryBookDate;
        private Timestamp statusModifiedAt;
        private String status;
        private String reason;
        private String hanClass;
        private String bookClass;
        private Timestamp createdAt;
        private String parentTel;

    }

    @Data
    public static class StudentInOutDTO {
        private Integer studentNo;
        private String studentId;
        private String studentName;
        private String hanClass;
        private String bookClass;
        private String grade;
        private String moveAt;
        private String transferReson;
        private Timestamp createdAt;
        private String hanTeacher;
        private String bookTeacher;
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

}
