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
}
