package com.hohoedu.all_pass.user._dto;

import lombok.Data;

public class UserReqDTO {

    @Data
    public static class UserLoginDTO {
        private String centerCode;
        private String userId;
        private String userPassword;
    }

    @Data
    public static class PasswordChangeRequest {
        private String userCode;
        private String newPassword;
    }
}
