package com.hohoedu.all_pass.logistics._dto;

import lombok.Data;

import java.util.List;

@Data
public class LogisReqDTO {

    @Data
    public static class DeadlineUpdateReqDTO {
        private String centerCode;
        private int deadlineAt;
    }

    @Data
    public static class ReorderListReqDTO {
        private String year;
        private String month;
        private String centerCode;
        private boolean onlyWait;
    }

    @Data
    public static class AggregateReqDTO {
        private String year;
        private String month;
        private String segmentType; // "전체 집계" | "센터별 집계" | "센터별 선생님 집계"
        private List<String> centerCodes;
    }

    @Data
    public static class TeacherOrderHistoryReqDTO {
        private String centerCode;
        private String userCode;
        private String year;
        private String month;
    }

    @Data
    public static class ConfirmedUpdateReqDTO {
        private String id;
        private String confirmed;
        private String centerCode;
    }

    @Data
    public static class OrderManualSaveReqDTO {
        private String yy;
        private String mm;
        private String title;
        private String centerCode;
        private String writerName;
        private String totalAmount;
        private List<OrderManualItemDTO> items;

        @Data
        public static class OrderManualItemDTO {
            private String orderDate;
            private String classKey;
            private String unitKey;
            private String qty;
            private String price;
            private String amount;
            private String note;
            private String approveDate;
        }
    }

    @Data
    public static class ManualListReqDTO {
        private String year;
        private String month;
        private String centerCode;
    }

    @Data
    public static class ManualItemsReqDTO {
        private String manualId;
    }

    @Data
    public static class ManualAddOptionsReqDTO {
        private String centerCode;
    }

    @Data
    public static class UnitCodeByClassKeyReqDTO {
        private String classKey;
    }

    @Data
    public static class ReorderManualAddReqDTO {
        private String year;
        private String month;
        private String centerCode;
        private List<ReorderAddItemDTO> items;

        @Data
        public static class ReorderAddItemDTO {
            private String classKey;
            private String unitKey;
            private String qty;
            private String userCode;
            private String reason;
        }
    }

    @Data
    public static class ManualItemAddReqDTO {
        private String manualId;
        private List<OrderManualSaveReqDTO.OrderManualItemDTO> items;
    }
}
