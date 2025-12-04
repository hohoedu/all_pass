package com.hohoedu.all_pass.admin.ebook._dto;

import lombok.Data;

import java.util.List;

@Data
public class EbookReqDTO {
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
}
