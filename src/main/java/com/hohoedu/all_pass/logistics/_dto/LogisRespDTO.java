package com.hohoedu.all_pass.logistics._dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class LogisRespDTO {

    @Data
    public static class DeadlineDTO {
        private String centerCode;
        private int deadlineAt;
    }

    @Data
    public static class ReorderStatusDTO{
        private String centerCode;
        private int requestCount;
        private boolean confirmed;
    }

    @Data
    public static class SelectCenterDTO{
        private List<ReorderListDTO> reorderList;
        private List<SummaryInvoiceDTO>  summaryInvoice;
        private CenterInfoDTO centerInfo;

        @Data
        public static class ReorderListDTO {
            private Integer id;
            private String state;
            private String userName;
            private String className;
            private String unitName;
            private String cnt;
            private String confirmed;
            private String centerCode;
            private String createdAt;
            private String reason;
        }

        @Data
        public static class SummaryInvoiceDTO {
            private String yyMm;
            private String itemCount;
            private String totalCount;
        }

        @Data
        public static class CenterInfoDTO {
            private String centerName;
            private String bizNum;
            private String directorName;
            private String address;
            private String managerName;
            private String managerTel;
        }
    }



    @Data
    public static class InvoiceDTO {
        private String rowType;
        private String orderDate;
        private String className;
        private String unitName;
        private int totalCount;
        private int unitPrice;
        private int totalPrice;
        private String userName;
    }

    @Data
    public static class CenterAggregateDTO {
        private String centerCode;
        private String centerName;
        private List<AggregateItemDTO> items;

        @Data
        public static class AggregateItemDTO {
            private String userCode;
            private String userName;
            private String className;
            private String unitName;
            private int baseCount;
            private int teacherCount;
            private int reorderCount;
            private int totalCount;
            private int timeTableCount;
        }
    }


    @Data
    public static class ReorderSugiItemDTO {
        private String classKey;
        private String unitKey;
        private String qty;
        private String userCode;
        private String reason;
    }
}
