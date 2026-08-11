package com.hohoedu.all_pass.third._dto;

import lombok.Data;

@Data
public class ThirdDTO {

    /** hohosc_TableBookLabel 에 이미 생성되어 있는 이북 코드 */
    @Data
    public static class KeycodeRawDTO {
        private String keycode;  // 이북 코드
        private String ggubun;   // 교재 코드 (erp_secondary_class_map.ext_code 와 같은 체계)
        private String mgubun;   // 호수 (인물 교재는 01~30, 호수 교재는 01~15)
        private String orderym;  // 주문 연월 (yyyyMM)
    }
}
