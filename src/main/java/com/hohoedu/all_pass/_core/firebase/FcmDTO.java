package com.hohoedu.all_pass._core.firebase;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FcmDTO {

    @Data
    public static class SingleFcmDTO {
        private String token;
        private String title;
        private String body;
    }

    @Data
    public static class MultiFcmDTO {
        private List<String> tokens;
        private String title;
        private String body;
    }

    @Data
    public static class InfantFcmDTO {

        private List<StudentTokenDTO> students;

        private String title;
        private String body;

        private String classType;
        private String timeTableKey;

        @Data
        public static class StudentTokenDTO {
            private String studentId;
            private String token;
        }
    }
}


