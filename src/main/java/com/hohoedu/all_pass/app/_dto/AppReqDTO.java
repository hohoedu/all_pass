package com.hohoedu.all_pass.app._dto;

import lombok.Data;

@Data
public class AppReqDTO {

    @Data
    public static class CalendarReqDTO {
        private String year;
        private String month;
        private String centerCode;
    }
}
