package com.hohoedu.all_pass.class_instance._dto.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

public class ClassRespDTO {

    @Data
    public static class TimeTableLabelDTO {
        private String timeTableKey;
        private String classLabel;
        private String classTime;
        private String classSubject;
    }

    @Data
    public static class TimeTableDTO {
        private String timeTableKey;
        private String periodNo;
        private String startTime;
        private String endTime;
        private String yy;
        private String mm;
        private String dayname;
        private String className;
        private String unitName;
        private String gradeName;
        private String classKey;
        private String unitKey;
        private String gradeKey;
        private String classType;

        private List<StudentDTO> students = new ArrayList<>();

        @Data
        public static class StudentDTO {
            private String studentId;
            private String studentName;
            private String week;
        }
    }

    @Data
    public static class ClassInfoDTO {
        private String classType;
        private String classKey;
        private String centerCode;
        private String userCode;
    }

    @Data
    public static class RemedialDTO {
        private String remedialKey;
        private String studentName;
        private String userName;
        private String remedialSubject;
        private String absenceDate;
        private String remedialDate;
        private boolean action;
        private Timestamp updatedAt;
    }

    @Data
    public static class InitRecordDTO {
        private String timeTableKey;
        private String classLabel;
        private String classKey;
        private String unitKey;
    }

    @Data
    public static class RecordLabelDTO {
        private String timeTableKey;
        private String classLabel;
        private String classKey;
        private String unitKey;
    }


    @Data
    @AllArgsConstructor
    public static class RecordBundleDTO {
        private List<RecordStudentDTO> students;
        private AfterClassRespDTO afterClass;
    }

    @Data
    public static class RecordStudentDTO {
        private String studentId;
        private String studentName;
        private String appToken;
        private String timeTableKey;
        private String ym;
        private String classKey;
        private String unitKey;
        private String week;
        private String inTime;
        private String outTime;
        private String attendanceName;
        private String absenceDate;
        private String remedialDate;
        private String centerCode;
        private String isBeforeSend;
        private String isAfterSend;
        private String updatedAt;
    }

    @Data
    public static class BeforeClassRespDTO {
        private String beforeClassKey;
        private String content;
        private String timeTableKey;
        private String timeTableLabel;
        private String userName;
    }

    @Data
    public static class AfterClassRespDTO {
        private String afterClassKey;
        private String content;
        private String timeTableKey;
        private String timeTableLabel;
        private String userName;
    }

    @Data
    public static class MonthlyStudentDTO {
        private String studentId;
        private String studentName;
        private String timeTableKey;
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

            public List<Boolean> getQuestionList() {
                return Arrays.asList(
                        question1, question2, question3, question4,
                        question5, question6, question7, question8);
            }
        }
    }

    @Data
    public static class RawClassDTO {
        private String classType;
        private String yy;
        private String mm;
        private String dayname;
        private String startTime;
        private String className;
        private String unitName;
        private String userName;
    }

}
