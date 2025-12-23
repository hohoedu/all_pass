package com.hohoedu.all_pass.manage._dto;

import lombok.Data;

import java.sql.Timestamp;

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

    @Data
    public static class ReorderListDTO {
        private Integer id;
        private String reorderType;
        private String classKey;
        private String className;
        private String unitKey;
        private String unitName;
        private String reason;
        private Integer count;
        private String confirmed;
        private String createdAt;

    }
}
