package com.hohoedu.all_pass.user._dto;

import com.fasterxml.jackson.core.ErrorReportConfiguration;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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

    @Data
    public static class UserRegisterDTO {
        private String userId;
        private String password;
        private String userName;
        private String phone;
        private String roleKey;
        private boolean useYn;
        private boolean han;
        private boolean book;
        private boolean clinic;
    }

    @Getter
    @Builder
    public static class UserInsert {
        private String userCode;
        private String userId;
        private String userName;
        private String userPhone;
        private String passwordHash;
        private String salt;
        private String type;
        private Boolean useYn;
        private Boolean han;
        private Boolean book;
        private Boolean clinic;
        private String roleKey;
        private String centerCode;
    }

    @Data
    @Builder
    public static class MenuAuthInsert {
        private String userCode;
        private String menuId;
        private boolean canRead;
        private boolean canWrite;
        private boolean canDelete;
    }
}
