package com.hohoedu.all_pass.class_instance._dto.web;

import java.util.List;

import lombok.Data;

@Data
public class ClassReqDTO {

    @Data
    public static class ClassRegisterDTO {
        private Integer id;
        private String yy;
        private String mm;
        private String dayname;
        private String periodNo;
        private String startTime;
        private String endTime;
        private String classKey;
        private String unitKey;
        private String gradeKey;
        private String userCode;
        private String timeTableKey;
        private String centerCode;
    }

    @Data
    public static class AddStudentDTO {
        private Integer id;
        private String weekNo;
        private String studentId;
        private String timeTableKey;
        private String centerCode;
    }

    @Data
    public static class AddStudentList {
        private List<AddStudentDTO> assignments;
    }

    @Data
    public static class ClassRecordByDateDTO {
        private String classCode;
        private String yy;
        private String mm;
        private String day;
        private String date;
    }

    @Data
    public static class ClassRecordReqDTO {
        private String userCode;
        private String yy;
        private String mm;
        private String day;
        private String date;
        private String classKey;
        private String unitKey;
        private String week;
        private String timeTableKey;
    }
    @Data
    public static class createAttendanceDTO {
        private String studentId;
        private String classKey;
        private String unitKey;
        private String week;
        private String timeTableKey;
        private String centerCode;
        private String attendanceDate;
    }


    @Data
    public static class BeforeClassDTO {
        private String classKey;
        private String unitKey;
        private String week;
        private String timeTableKey;
    }

    @Data
    public static class AfterClassDTO {
        private String classKey;
        private String unitKey;
        private String week;
        private String timeTableKey;
    }

    @Data
    public static class UpdateRemedialDTO {
        private String remedialKey;
        private boolean isAction;
    }

    @Data
    public static class UpdateRemedialDateDTO {
        private String remedialKey;
        private String remedialDate;
    }


    @Data
    public static class ClassMonthlyByMonthDTO {
        private String yy;
        private String mm;
    }

    @Data
    public static class ClassMonthlyByClassCodeDTO {
        private String classCode;
    }

    @Data
    public static class ClassMonthlyScoreDTO {
        private String studentId;
        private String classCode;
        private String yy;
        private String mm;
        private List<MonthlyScoreDTO> scores;

        @Data
        public static class MonthlyScoreDTO {
            private boolean question1;
            private boolean question2;
            private boolean question3;
            private boolean question4;
            private boolean question5;
            private boolean question6;
            private boolean question7;
            private boolean question8;
        }
    }

}
