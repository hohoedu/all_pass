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
    public static class AppTokenReqDTO {
        private String token;
        private String id;
        private String state;
    }
}
