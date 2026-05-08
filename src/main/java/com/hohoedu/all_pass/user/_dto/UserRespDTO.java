package com.hohoedu.all_pass.user._dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class UserRespDTO {

    @Data
    public static class UserAuthDTO {
        private String userId;
        private String userCode;
        private String passwordHash;
        private String centerCode;
        private String salt;
        private Boolean useYn;

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
        private String type;
        private String centerCode;
        private String centerName;
        private String regionName;
        private Integer roleNum;
        private List<String> readableMenus;

        @Builder
        public LoginRespDTO(String userCode, String userId, String userName, String roleKey, String type,
                String centerCode, String centerName, String regionName, Integer roleNum) {
            this.userCode = userCode;
            this.userId = userId;
            this.userName = userName;
            this.roleKey = roleKey;
            this.type = type;
            this.centerCode = centerCode;
            this.centerName = centerName;
            this.regionName = regionName;
            this.roleNum = roleNum;
        }

    }

    @Data
    public static class UserListRespDTO {
        private String userCode;
        private String userId;
        private String userName;
        private String roleKey;
        private String userPhone;
        private Boolean useYn;
        private Boolean isHan;
        private Boolean isBook;
        private Timestamp createdAt;
    }

    @Data
    public static class MenuListDTO {
        private String menuId;
        private String menuName;
        private String parentId;
        private String menuType;
    }

}
