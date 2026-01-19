package com.hohoedu.all_pass.class_instance._dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public static class MonthlyReportRespDTO {
        private String part1 = "";
        private String part2 = "";
        private String part3 = "";
        private String part4 = "";
        private String part5 = "";
        private String part6 = "";
        private String part7 = "";
        private String part8 = "";
        @JsonProperty("part1_title")
        private String part1Title;
        @JsonProperty("part2_title")
        private String part2Title;
        @JsonProperty("part3_title")
        private String part3Title;
        @JsonProperty("part4_title")
        private String part4Title;
        @JsonProperty("part5_title")
        private String part5Title;
        @JsonProperty("part6_title")
        private String part6Title;
        @JsonProperty("part7_title")
        private String part7Title;
        @JsonProperty("part8_title")
        private String part8Title;
        @JsonProperty("part1_level")
        private String part1Level;
        @JsonProperty("part2_level")
        private String part2Level;
        @JsonProperty("part3_level")
        private String part3Level;
        @JsonProperty("part4_level")
        private String part4Level;
        @JsonProperty("part5_level")
        private String part5Level;
        @JsonProperty("part6_level")
        private String part6Level;
        @JsonProperty("part7_level")
        private String part7Level;
        @JsonProperty("part8_level")
        private String part8Level;
        @JsonProperty("review")
        private String review = "";
        @JsonProperty("class_contents")
        private String classContents = "";
        @JsonProperty("result_contents")
        private String resultContents = "";
        @JsonProperty("partnote")
        private String partnote = "";
        @JsonProperty("sdate")
        private String sdate = "";

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

    @Data
    public static class MonthlyHaniRespDTO {

        @JsonProperty("main_title")
        private String mainTitle;

        private String subtitle;

        private String title1;
        private String title2;
        private String title3;

        private String img;

        @JsonProperty("part1_title")
        private String part1Title;

        @JsonProperty("part2_title")
        private String part2Title;

        @JsonProperty("part3_title")
        private String part3Title;

        @JsonProperty("part4_title")
        private String part4Title;

        @JsonProperty("part5_title")
        private String part5Title;

        @JsonProperty("part1_note")
        private String part1Note;

        @JsonProperty("part2_note")
        private String part2Note;

        @JsonProperty("part3_note")
        private String part3Note;

        @JsonProperty("part4_note")
        private String part4Note;

        @JsonProperty("part5_note")
        private String part5Note;

        @JsonProperty("part1_tag_1")
        private String part1Tag1;

        @JsonProperty("part1_tag_2")
        private String part1Tag2;

        @JsonProperty("part1_tag_3")
        private String part1Tag3;

        @JsonProperty("part2_tag_1")
        private String part2Tag1;

        @JsonProperty("part2_tag_2")
        private String part2Tag2;

        @JsonProperty("part2_tag_3")
        private String part2Tag3;

        @JsonProperty("part3_tag_1")
        private String part3Tag1;

        @JsonProperty("part3_tag_2")
        private String part3Tag2;

        @JsonProperty("part3_tag_3")
        private String part3Tag3;

        @JsonProperty("part4_tag_1")
        private String part4Tag1;

        @JsonProperty("part4_tag_2")
        private String part4Tag2;

        @JsonProperty("part4_tag_3")
        private String part4Tag3;

        @JsonProperty("part5_tag_1")
        private String part5Tag1;

        @JsonProperty("part5_tag_2")
        private String part5Tag2;

        @JsonProperty("part5_tag_3")
        private String part5Tag3;

        private String sdate;
    }


}
