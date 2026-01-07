package com.hohoedu.all_pass.class_instance._dto.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class ClassRespDTO {

    @Data
    public static class MainClassSummaryDTO {
        private String className;
        private String startTime;
        private String userName;
        private String timeTableKey;
        private String classKey;
        private String countStudent;
    }

    @Data
    public static class ClassWeekDTO {
        private Integer id;
        private String year;
        private String month;
        private String week;
        private String mon;
        private String tue;
        private String wed;
        private String thu;
        private String fri;
        private String sat;
        private String sun;
    }

    @Data
    public static class ClassUnitDTO {
        private String classKey;
        private String unitKey;
        private String unitName;
    }

    @Data
    public static class TimeTableLabelDTO {
        private String timeTableKey;
        private String classKey;
        private String unitKey;
        private String classLabel;
        private String classTime;
        private String classSubject;
        private String yy;
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
        private String subUnitKey;
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
    public static class ComClassStudentDTO {
        private String studentId;
        private String studentName;
        private String timeTableKey;
        private String gradeKey;
        private String gradeName;
        private String classKey;
        private String unitKey;
        private String className;
        private String unitName;
    }

    @Data
    public static class ClassInfoDTO {
        private String timeTableKey;
        private String classType;
        private String classKey;
        private Integer classFee;
        private Integer bookFee;
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
    public static class TimeRangeDTO {
        private String timeTableKey;
        private String startTime;
        private String endTime;
        private String dayname;
    }

    @Data
    public static class FinishClassDTO {
        private String id;
        private String yy;
        private String mm;
        private String dayname;
        private String periodNo;
        private String startTime;
        private String endTime;
        private String timeTableKey;
        private String createdAt;
        private String updatedAt;
        private String userCode;
        private String className;
        private String week;
        private String centerCode;
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
        private List<AfterClassRespDTO> afterClass;
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
        private String word;
        private String timeTableKey;
        private String timeTableLabel;
        private String userName;
        private String review;
    }

    @Data
    public static class MonthlyStudentDTO {
        private String studentId;
        private String studentName;
        private String timeTableKey;
        private String feedback;
        private String bottomComment;
        private boolean isSend;
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
    public static class ScoreResultDTO {
        private String timeTableKey;
        private String studentId;
        private String studentName;
        private String scoreResult;
    }

    @Data
    public static class MonthlyPreviewRespDTO {
        private String studentId;
        private String studentName;
        private String timeTableKey;
        private String topComment;
        private String bottomComment;
        private String feedback;
        private List<String> scores;
        private List<String> competency;
        private List<String> difficultly;
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

    @Data
    public static class InfantHanDTO {
        private String classLabel;
        private String imagePath;
        private String subject;
        private String newWord;
        private String story;
        private String subStory;
        private String idiom;
        private String subIdiom;
        private String hanjaSong;
        private String workBook;
        private String storyComment;
        private String clean;
        private String insung;

        private List<StudentInfo> students;

        @Data
        public static class StudentInfo {
            private String studentId;
            private String studentName;
            private String appId;
            private String appToken;

        }
    }

    @Data
    public static class InfantBookDTO {
        private String classLabel;
        private String subject;
        private String imagePath;
        private String content;
        private String story;
        private String knowledgeBoard;
        private String thinkTalk;
        private String goldenbell;
        private String findDiff;

        private List<StudentInfo> students;

        @Data
        public static class StudentInfo {
            private String studentId;
            private String studentName;
            private String appId;
            private String appToken;

        }
    }

    @Data
    public static class BasicTimeTableInfo {
        private String classKey;     // 시간표의 class_key
        private String teacherCode;  // user_code
        private String classType;    // 한자 = 1, 독서 = 2
    }

}
