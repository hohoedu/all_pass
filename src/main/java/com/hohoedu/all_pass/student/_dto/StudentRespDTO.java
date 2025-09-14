package com.hohoedu.all_pass.student._dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class StudentRespDTO {

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
        private String center;
        private String entryHanDate;
        private String entryBookDate;
        private String status;
        private String statusName;
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
        private String statusName;
        private String reason;
        private String hanClass;
        private String bookClass;
        private Timestamp createdAt;
        private String parentTel;

    }

    @Data
    public static class StudentFilterDTO {
        private Integer studentNo;
        private String studentName;
        private String school;
        private String grade;
        private String centerNo;
        private String entryDate;
        private String status;
        private String hanClass;
        private String bookClass;
        private String isSibling;
    }

    @Data
    public static class MainStudentDTO {
        private String studentId;
        private String studentName;
        private String statusName;
        private String entryDate;
        private String gradeName;
        private String school;
        private String centerName;
        private String hanClass;
        private String bookClass;
    }


    @Data
    public static class StudentInOutDTO {
        private String studentId;
        private String studentName;
        private String hanClass;
        private String bookClass;
        private String gradeName;
        private String moveAt;
        private String transferReson;
        private String entryDate;
        private String hanTeacher;
        private String bookTeacher;
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

}
