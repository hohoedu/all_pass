package com.hohoedu.all_pass.class_instance._dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public class ClassRespDTO {

    @Data
    public static class TimeTableDTO {
        private Integer timeTableNo;
        private String periodNo;
        private String startTime;
        private String endTime;
        private String yy;
        private String mm;
        private String dayname;
        private String classType;
        private String className;
        private String unitCode;
        private Integer classNo;
        private Integer unitNo;
        private Integer gradeNo;

        private List<StudentDTO> students = new ArrayList<>();

        @Data
        public static class StudentDTO {
            private Integer studentNo;
            private String studentName;
            private Integer timeTableAssignNo;
        }
    }
}
