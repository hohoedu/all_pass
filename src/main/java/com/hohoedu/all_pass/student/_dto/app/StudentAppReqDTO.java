package com.hohoedu.all_pass.student._dto.app;

import lombok.Data;

@Data
public class StudentAppReqDTO {

    @Data
    public static class LoginReqDTO {
        private String id;
        private String sha_pwd;
    }

    @Data
    public static class StudentAttendanceDTO {
        private String centerCode;
        private String studentId;
        private String ymd;
        private String hhmm;
        private String attendType;
    }

    @Data
    public static class AttendanceTokenDTO {
        private String appId;
    }

    @Data
    public static class AppTokenReqDTO {
        private String token;
        private String id;
        private String state;
    }
}
