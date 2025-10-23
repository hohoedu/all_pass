package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.payment._dto.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.ClassFeeMap;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
//        System.out.println(reqDTO.getAppr_state());
        System.out.println(rawJson);
        return rawJson;
    }

    @PostMapping("/students")
    public ResponseEntity<?> getStudents(HttpSession session, @RequestBody PaymentReqDTO.StudentsByMonthDTO paymentReqDTO) {

        String year = paymentReqDTO.getYear();
        String month = paymentReqDTO.getMonth();
        String userCode = paymentReqDTO.getUserCode();
        System.out.println("year : " + year);
        System.out.println("month : " + month);
        System.out.println("userCode : " + userCode);

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentService.findByAssignStudent(year, month, userCode);

        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @GetMapping("/fee/{classKey}")
    @ResponseBody
    public ResponseEntity<?> getClassFee(HttpSession session, @PathVariable String classKey) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        Integer result = paymentService.findFeeByClassKey(classKey, user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(result));
    }


}

