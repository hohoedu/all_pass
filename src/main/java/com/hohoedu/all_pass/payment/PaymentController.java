package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.payment._dto.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
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

    @PostMapping("/history/insert")
    public ResponseEntity<?> updatePayment(@RequestBody PaymentReqDTO.PayHistoryDTO paymentReqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        System.out.println("============================================");
        System.out.println("billId" + paymentReqDTO.getBillId());
        System.out.println("productName" + paymentReqDTO.getProductName());
        System.out.println("amount" + paymentReqDTO.getAmount());
        System.out.println("status" + "issued");
        System.out.println("message" + paymentReqDTO.getMessage());
        System.out.println("requestDate" + paymentReqDTO.getRequestDate());
        System.out.println("studentId" + paymentReqDTO.getStudentId());
        System.out.println("userCode" + user.getUserCode());
        System.out.println("centerCode" + user.getCenterCode());
        System.out.println("============================================");

        paymentReqDTO.setPaymentStatus("issued");
        paymentReqDTO.setUserCode(user.getUserCode());
        paymentReqDTO.setCenterCode(user.getCenterCode());

        paymentService.insertPayment(paymentReqDTO);

        return ResponseEntity.ok(ApiUtils.success(null));
    }


}
