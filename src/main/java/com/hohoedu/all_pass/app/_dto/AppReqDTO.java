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

    @Data
    public static class QnaReqDTO {
        private String gubun;
    }

    @Data
    public static class NotiOptionViewReqDTO {
        private String stuid;
    }

    @Data
    public static class NotiOptionSaveReqDTO {
        private String stuid;
        private java.util.List<NotiOptionItem> options;

        @Data
        public static class NotiOptionItem {
            private String typeCode;
            private String enabled; // "Y" / "N"
        }
    }
}
