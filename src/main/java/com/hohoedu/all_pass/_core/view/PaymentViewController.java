package com.hohoedu.all_pass._core.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentViewController {

    @GetMapping("/pay-edu")
    public String payEdu() {
        return "pay/pay-edu";
    }

    @GetMapping("/pay-list")
    public String payList() {
        return "pay/pay-list";
    }
    
}
