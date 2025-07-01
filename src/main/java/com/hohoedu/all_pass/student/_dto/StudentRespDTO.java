package com.hohoedu.all_pass.student._dto;

import java.sql.Timestamp;

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

}
