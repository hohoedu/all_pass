package com.hohoedu.all_pass.secondary._dto;

import lombok.Data;
import java.util.List;

@Data
public class SecondaryDTO {

    @Data
    public static class TeacherDTO {
        private String userCode;
        private String userName;
    }

    @Data
    public static class TimetableRawDTO {
        private String timelevel;
        private Integer daynumber;
        private String stime;
        private String etime;
        private String className;
        private String unitName;
        private String gb;
    }

    @Data
    public static class TimetableDTO {
        private String periodNo;
        private String dayname;
        private String startTime;
        private String endTime;
        private String className;
        private String unitName;
        private String classType;
        private List<Object> students = new java.util.ArrayList<>();
    }

    @Data
    public static class TestDTO {
        private String yyyy;
        private String mm;
        private String ggubun;
        private String ggubunStr;
        private String mgubun;
        private String mgubunStr;
        private String qty;
        private String tqty;
        private String idate;
        private String gb;
        private String teaid;
        private String danga;
        private String gbSort;
        private String teaidname;
    }
}
