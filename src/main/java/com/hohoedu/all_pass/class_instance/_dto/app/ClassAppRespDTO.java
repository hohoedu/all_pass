package com.hohoedu.all_pass.class_instance._dto.app;

import lombok.Data;

import java.util.List;

@Data
public class ClassAppRespDTO {

    @Data
    public static class ClassInfoRespDTO {
        private String gubun;
        private String note;
        private String dayname;
        private String mm;
        private String stime;
        private String etime;

    }

    @Data
    public static class BookListRawDTO {
        private String hakInfo;
        private String weekSubject;
        private String weekTitle;
        private String weekPublisher;
        private String weekBookimg;
    }

    @Data
    public static class BookListRespDTO {
        private String hak_info;
        private List<BookList> books;

        @Data
        public static class BookList {
            private String week_subject;
            private String week_title;
            private String week_publisher;
            private String week_bookimg;
        }
    }

    @Data
    public static class BookListMainRawDTO {
        private String hakInfo;
        private String yyyy;
        private String mm;
        private String weekSubject;
        private String weekTitle;
        private String weekPublisher;
        private String weekBookimg;
    }

    @Data
    public static class BookListMainRespDTO {
        private String hak_info;
        private String yyyy;
        private String mm;
        private List<BookMainList> data;

        @Data
        public static class BookMainList {
            private String week_subject;
            private String week_title;
            private String week_publisher;
            private String week_bookimg;
        }
    }

    @Data
    public static class BeforeClassRespDTO {
        private String gubun;
        private String note;        // classLabel
        private String dayname;
        private String ymd;         // classDate
        private String studytime;   // classTime;
        private String prequest;    // content;
    }

    @Data
    public static class AfterClassRespDTO {
        private String gubun;
        private String title;        // classLabel
        private String dayname;
        private String gbcd;         // classDate
        private String mgubun;   // classTime;
        private String ju;    // content;
        private String yyyy;    // content;
        private String mm;    // content;
        private String icon;    // content;
        private String snote;    // content;
    }

    @Data
    public static class AfterClassDetailRespDTO {
        private String gamok;
        private String title;
        private String dayname;
        private String ju_note1;
        private String ju_note2;
        private String review;
    }

    @Data
    public static class ClinicResultRespDTO {
        private String qtypestr;
        private String per;
        private String ranking;
        private String result;
    }

    @Data
    public static class ClinicListRespDTO {
        private String title;
        private String sdate;
    }

    @Data
    public static class ClinicTotalRespDTO {
        private String ym;
        private String cnt;
    }

}
