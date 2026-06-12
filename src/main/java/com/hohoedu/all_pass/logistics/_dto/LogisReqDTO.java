package com.hohoedu.all_pass.logistics._dto;

import lombok.Data;
import lombok.Getter;

@Data
public class LogisReqDTO {

    @Data
    public static class ReorderListReqDTO {
        private String year;
        private String month;
        private String centerCode;
        private boolean onlyWait;
    }
}
