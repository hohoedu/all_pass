package com.hohoedu.all_pass.payment._dto.web;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PaymentReqDTO {

    @Data
    public static class PayCallbackDTO {
        private String apikey;
        private String bill_id;
        private String appr_pay_type;
        private String appr_card_type;
        private String appr_dt;
        private String appr_issuer;
        private String appr_num;
        private String appr_price;
        private String appr_state;
    }

    @Data
    public static class StudentsByMonthDTO {
        private String year;
        private String month;
        private String userCode;
    }




}
