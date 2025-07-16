package com.hohoedu.all_pass.class_instance._dto;

import java.util.List;

import lombok.Data;

@Data
public class ClassReqDTO {

    @Data
    public static class ClassRegisterDTO {
        private Integer timeTableNo;
        private String yy;
        private String mm;
        private String dayname;
        private String periodNo;
        private String startTime;
        private String endTime;
        private String classNo;
        private String unitNo;
        private String gradeNo;
        private String userNo;
    }

    @Data
    public static class AddStudentDTO {
        private Integer timeTableAssignNo;
        private String weekNo;
        private Integer studentNo;
        private Integer timeTableNo;
    }

    @Data
    public static class AddStudentList {
        private List<AddStudentDTO> assignments;
    }
}
