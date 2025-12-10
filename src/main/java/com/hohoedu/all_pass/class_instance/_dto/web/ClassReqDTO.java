package com.hohoedu.all_pass.class_instance._dto.web;

import java.util.List;
import java.util.Map;

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
    public static class AddStudentList {
        private List<AddStudentDTO> assignments;
    }

    @Data
    public static class AddStudentDTO {
        private Integer id;
        private String weekNo;
        private String studentId;
        private String timeTableKey;
        private String yy;
        private String mm;

    }

    @Data
    public static class DeleteTimeTableDTO {
        private String timeTableKey;
        private String year;
        private String month;
    }

    @Data
    public static class AssignUpdateDTO {
        private String timeTableKey;
        private List<StudentInfo> studentInfos;

        @Data
        public static class StudentInfo {
            private String studentId;
            private String classKey;
            private String unitKey;
            private String yy;
            private String mm;
        }
    }

    @Data
    public static class SetWeekDTO {
        private String year;
        private String month;
        private String week;
        private String centerCode;
        private String mon;
        private String tue;
        private String wed;
        private String thu;
        private String fri;
        private String sat;
        private String sun;
    }

    @Data
    public static class WeekReqDTO {
        private String year;
        private String month;
        private Map<String, WeekDetailDTO> week;

        @Data
        public static class WeekDetailDTO {
            private String mon;
            private String tue;
            private String wed;
            private String thu;
            private String fri;
            private String sat;
            private String sun;
        }
    }

    @Data
    public static class GetWeekDTO {
        private String year;
        private String month;
        private String centerCode;
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
    public static class updateAttendanceDTO {
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
    public static class TeacherAssignUpdateDTO {
        private String studentId;

        private boolean hanState;
        private String hanTeacher;
        private String hanEntryDate;
        private Integer hanMaterialFee;
        private String hanClass;

        private boolean bookState;
        private String bookTeacher;
        private String bookEntryDate;
        private Integer bookMaterialFee;
        private String bookClass;
    }

    @Data
    public static class ClassMonthlyDTO {
        private String yy;
        private String mm;
        private String dayname;
        private String userCode;
    }

    @Data
    public static class ClassMonthlyByClassCodeDTO {
        private String timeTableKey;
    }

    @Data
    public static class ClassMonthlyScoreDTO {
        private String studentId;
        private String timeTableKey;
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

    @Data
    public static class MonthlyPreviewDTO {
        private String studentId;
        private String timeTableKey;
        private String yy;
        private String mm;
    }

    @Data
    public static class InfantClassLabelsDTO {
        private String yy;
        private String mm;
        private String userCode;
    }

    @Data
    public static class InfantDetailDTO {
        private String classKey;
        private String classSubject;
        private String unitKey;
        private String timeTableKey;
        private String yy;
    }


    @Data
    public static class InfantSaveReqDTO {

        private String type;

        private Detail detail;

        private String classKey;
        private String unitKey;
        private String timeTableKey;

        private List<StudentDTO> students;

        @Data
        public static class StudentDTO {
            private String studentId;
            private String studentName;
        }

        @Data
        public static class Detail {
            private String classLabel;
            private String subject;
            private String imagePath;

            // BOOK
            private String content;
            private String story;
            private String knowledgeBoard;
            private String thinkTalk;
            private String goldenbell;
            private String findDiff;

            // HAN
            private String newWord;
            private String subStory;
            private String idiom;
            private String subIdiom;
            private String hanjaSong;
            private String workBook;
            private String storyComment;
            private String clean;
            private String insung;
        }
    }

    @Data
    public static class BeforeClassNoticeDTO {
        private String studentId;
        private String userCode;
        private String timeTableKey;
        private String classDate;
        private String week;
        private String dayname;
        private String classTime;
        private String content;
        private String classType;
        private String classLabel;
    }

    @Data
    public static class AfterClassNoticeDTO {
        private String studentId;
        private String userCode;
        private String timeTableKey;
        private String afterClassKey;
        private String year;
        private String month;
        private String week;
        private String dayname;
        private String content;
        private String classType;
        private String classLabel;
        private String icon;
    }

    // 시간표 조회 Controller 요청 dto
    @Data
    public static class TimeTaleViewReqDTO {
        private String year;
        private String month;
        private String userCode;
    }

}
