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

    @Data
    public static class SavedOrderListDTO {
        private Integer baseCount;
        private Integer addCount;
        private Integer totalCount;
        private String className;
        private String classKey;
        private String unitName;
        private String unitKey;
        private String yy;
        private String mm;

    }
}
