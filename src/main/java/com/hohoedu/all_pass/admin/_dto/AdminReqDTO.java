package com.hohoedu.all_pass.admin._dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminReqDTO {

    @Data
    public static class PersonSettingDTO {
        private String centerCode;       // "PUS001"
        private String year;             // "2025"
        private List<EbookClassDTO> classes;  // K, M, J

        @Data
        public static class EbookClassDTO {
            private String class_key;            // "K", "M", "J"
            private List<EbookMonthDTO> months;  // size = 12

            @Data
            public static class EbookMonthDTO {
                private String month;        // "01" ~ "12"
                private String unit_key;     // 메인
                private String sub_unit_key; // 서브
            }
        }
    }

    @Data
    public static class PersonFindDTO {
        private String centerCode;
        private String year;
    }

    @Data
    public static class BookSuggestSaveReqDTO {

        private String classKey;   // 7세, 새움, 다움...
        private String yy;         // 연도
        private String mm;         // 월

        private List<WeekDTO> weeks;

        @Data
        public static class WeekDTO {
            private String week;        // 1 ~ 4
            private String subjectKey;  // 코드테이블 키값
            private String publisher;
            private String bookName;
            private String imageUrl;
        }
    }

    @Data
    public static class BookSuggestDTO {
        private String classKey;
        private String yy;
        private String mm;
        private String week;
        private String subjectKey;
        private String bookName;
        private String publisher;
        private String bookImageUrl;
    }
}
