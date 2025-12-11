package com.hohoedu.all_pass.popbill._dto;

import lombok.Data;

@Data
public class PopbillReqDTO {

    @Data
    public static class JoinNoticeRequest {

        private String centerCode;
        private String phone;
    }
}
