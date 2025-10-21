package com.hohoedu.all_pass.payment._dto;

import lombok.Data;

@Data
public class PaymentReqDTO {

    @Data
    public static class PayCallbackDTO {
        private String apikey;
        private String bill_id;
        private String appr_pay_type;
        private String appr_dt;
        private String appr_num;
        private String appr_price;
        private String appr_state;
    }
}
