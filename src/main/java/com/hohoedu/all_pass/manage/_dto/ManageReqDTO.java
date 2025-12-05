package com.hohoedu.all_pass.manage._dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class ManageReqDTO {

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

    @Data
    public static class GetOrderDTO {
        private String yy;
        private String mm;
        private String userCode;
        private String centerCode;
    }

    @Data
    public static class InsertReorderDTO {
        private String reorderType;
        private List<ReorderItemDTO> items;

        @Data
        public static class ReorderItemDTO {
            private String classKey;
            private String unitKey;
            private Integer count;
            private String reason;
        }
    }

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
