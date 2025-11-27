package com.hohoedu.all_pass.manage._dto;

import lombok.Data;

import java.util.List;

@Data
public class ManageReqDTO {

    @Data
    public static class InsertClassFeeDTO {
        private List<ClassFeeMapDTO> classFeeMap;

        @Data
        public static class ClassFeeMapDTO {
            private String centerCode;
            private Integer fee;
            private String classKey;

        }
    }

    @Data
    public static class InsertOrderDTO {
        private String classKey;
        private String unitKey;
        private String centerCode;
        private String userCode;
        private Integer baseCount;
        private Integer addCount;
        private Integer totalCount;
        private String yy;
        private String mm;
    }
}
