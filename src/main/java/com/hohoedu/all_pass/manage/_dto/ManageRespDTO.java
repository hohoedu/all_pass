package com.hohoedu.all_pass.manage._dto;

import lombok.Data;

@Data
public class ManageRespDTO {
    @Data
    public static class BasicOrderListDTO {
        private String className;
        private String classKey;
        private String unitName;
        private String unitKey;
        private String baseCount;
    }
}
