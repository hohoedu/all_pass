package com.hohoedu.all_pass.center._dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CenterRespDTO {

    @Data
    public static class PaymintConfigDTO {
        private String remainPointUrl;
        private String chargePointUrl;
        private String apiKey;
    }

    @Data
    public static class PaymintRemainRespDTO {
        private String code;
        private String msg;
        private Info info;

        @Data
        public static class Info {
            @JsonProperty("remain_count")
            private int remainCount;
        }
    }

    @Data
    public static class PointDTO {
        private Integer paymintPoint;
        private Integer popbillPoint;
        private String chargeUrl;
    }
}
