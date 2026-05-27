package com.hohoedu.all_pass.app._dto;

import lombok.Data;

@Data
public class AppRespDTO {

    @Data
    public static class ClassWeekDTO {
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
    public static class QnaRespDTO {
        private String question;
        private String answer;
    }

}
