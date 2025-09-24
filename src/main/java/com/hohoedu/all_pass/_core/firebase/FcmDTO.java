package com.hohoedu.all_pass._core.firebase;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FcmDTO {

    @Data
    public static class BeforeFcmDTO {
        private List<String> tokens;
        private String title;
        private String body;
    }
    @Data
    public static class AfterFcmDTO {
        private List<String> tokens;
        private String title;
        private String body;
    }

    @Data
    public static class AttendanceFcmDTO{
        private String token;
        private String title;
        private String body;
    }
}


