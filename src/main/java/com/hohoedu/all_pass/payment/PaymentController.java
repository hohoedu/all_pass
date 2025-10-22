package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass.payment._dto.PaymentReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    @PostMapping("/callback")
    @ResponseBody
    public String callback(@RequestBody String rawJson) {
        System.out.println("✅ 결제선생 콜백 도착");
//        System.out.println(reqDTO.getBill_id());
//        System.out.println(reqDTO.getApikey());
//        System.out.println(reqDTO.getAppr_pay_type());
//        System.out.println(reqDTO.getAppr_dt());
//        System.out.println(reqDTO.getAppr_num());
//        System.out.println(reqDTO.getAppr_price());
        System.out.println(rawJson);
//        System.out.println(reqDTO.getAppr_state());
        return rawJson;
    }
}

