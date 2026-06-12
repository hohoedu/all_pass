package com.hohoedu.all_pass.logistics._dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class LogisRespDTO {

    @Data
    public static class ReorderListDTO {
        private Integer id;
        private String state;
        private String userName;
        private String className;
        private String unitName;
        private String cnt;
        private String confirmed;
        private String centerCode;
        private String createdAt;
    }
}
