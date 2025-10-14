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

    @Data
    public static class BookListReqDTO {
        private String bookCode;
    }

    @Data
    public static class ClinicBookReqDTO {

    }

    @Data
    public static class BeforeClassReqDTO {
        private String id;
        private int count;
    }
}
