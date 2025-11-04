package com.hohoedu.all_pass.payment._dto.app;

import lombok.Data;

@Data
public class PaymentAppReqDTO {
    @Data
    public static class PaymentDetailsReqDTO {
        private String studentId;
        private String snum;
        private String count;
    }
}
