package com.hohoedu.all_pass.class_instance._dto.app;

import lombok.Data;

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
    public static class BookListRespDTO {

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


}
