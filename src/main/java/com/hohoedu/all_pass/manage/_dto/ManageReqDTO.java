package com.hohoedu.all_pass.manage._dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class ManageReqDTO {

    @Data
    public static class InsertOrderHistoryDTO {
        private String centerCode;
        private String userCode;
        private String yy;
        private String mm;
        private Integer round;

        List<InsertOrder> insertOrders;

        @Data
        public static class InsertOrder {
            private String classKey;
            private String unitKey;
            private Integer baseCount;
            private Integer addCount;
            private Integer totalCount;
        }

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
        private String yy;
        private String mm;

        @Data
        public static class ReorderItemDTO {
            private String classKey;
            private String unitKey;
            private Integer count;
            private String reason;
        }
    }

    @Data
    public static class OrderListReqDTO {
        private String yy;
        private String mm;
    }

    @Data
    public static class CancelReorderDTO {
        private Integer id;
    }

    @Data
    public static class InsertClassFeeDTO {
        private List<ClassFeeMapDTO> classFeeMap;

        @Data
        public static class ClassFeeMapDTO {
            private String centerCode;
            private Integer fee;
            private String classKey;
            private String unitKey;

        }
    }

    @Data
    public static class PermissionReqDTO {
        private List<MenuPermissionDTO> permissions;

        @Data
        @Builder
        public static class MenuPermissionDTO {
            private String menuId;
            private boolean canRead;
            private boolean canWrite;
            private boolean canDelete;
        }
    }

    @Data
    public static class PermissionCopyReqDTO {
        private String sourceUserCode;
        private String targetUserCode;
    }

}
