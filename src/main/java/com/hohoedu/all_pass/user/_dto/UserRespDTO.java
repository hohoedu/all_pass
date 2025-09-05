package com.hohoedu.all_pass.user._dto;

import lombok.Builder;
import lombok.Data;

@Data
public class UserRespDTO {

    @Data
    public static class UserAuthDTO {
        private String userId;
        private String passwordHash;
        private String centerCode;

        @Builder
        public UserAuthDTO(String userId, String passwordHash, String centerCode) {
            this.passwordHash = passwordHash;
            this.userId = userId;
            this.centerCode = centerCode;
        }
    }

    @Data
    public static class LoginRespDTO {
        private String userCode;
        private String userId;
        private String userName;
        private String roleKey;
        private String centerCode;
        private String centerName;
        private String regionName;

        @Builder
        public LoginRespDTO(String userCode, String userId, String userName, String roleKey, String centerCode, String centerName, String regionName) {
            this.userCode = userCode;
            this.userId = userId;
            this.userName = userName;
            this.roleKey = roleKey;
            this.centerCode = centerCode;
            this.centerName = centerName;
            this.regionName = regionName;
        }

    }

}
