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
    public static class KeycodeRawDTO {
        private String className; // 교재명 (codezip.note)
        private String unitName;  // 호수명 (hohosc_code.note)
        private String ggubun;    // 교재 코드 (codezip.code2 - U1~U3, E1~E4, UA~UC, EA~EB)
        private String mgubun;    // 호수 코드 (hohosc_code.code)
    }

    @Data
    public static class EbookYearConfigDTO {
        private String yy;
        private String mm;      // "01" ~ "12"
        private String unitNo;     // code  - 메인, "01" ~ "30" (우리 unit_key A01 ~ C10 에 대응)
        private String subUnitNo;  // code2 - 서브
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
