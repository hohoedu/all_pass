package com.hohoedu.all_pass.class_instance._dto.app;

import lombok.Data;

@Data
public class ClassAppReqDTO {
    @Data
    public static class ClassInfoReqDTO {
        private String id;
        private String yyyy;
        private String mm;
    }

    @Data
    public static class LearningContentsReqDTO {
        private String id;
        private int count;
    }

    // 메인화면
    @Data
    public static class BookListMainReqDTO {
        private String ihak;
    }

    @Data
    public static class BooklistReqDTO {
        private String id;
        private String yyyy;
        private String mm;
    }

    // 도서 상세 화면
    @Data
    public static class ClinicBookReqDTO {
        private String id;
        private String yyyy;
        private String mm;
    }

    @Data
    public static class BeforeClassReqDTO {
        private String id;
        private int count;
    }

    @Data
    public static class ClinicBookListReqDTO {
        private String id;
        private String ym;
    }

    @Data
    public static class ClinicBookResultReqDTO {
        private String id;
        private String ym;
    }

    @Data
    public static class ClinicBookTotalListReqDTO {
        private String id;
        private String sym;
        private String eym;
    }
}
