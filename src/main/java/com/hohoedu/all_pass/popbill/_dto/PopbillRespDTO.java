package com.hohoedu.all_pass.popbill._dto;

import lombok.Data;

@Data
public class PopbillRespDTO {

    @Data
    public static class PopbillTemplateRespDTO {
        private String id;
        private String popbillTemplateCode;
        private String templateName;
        private String content;
        private String status;
        private String isActive;
    }

}
