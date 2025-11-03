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
import java.util.Map;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody PaymentReqDTO.PayCallbackDTO dto) {
        System.out.println("✅ 결제선생 콜백 도착");

        paymentService.insertPaymentCallback(dto);

        return ResponseEntity.ok(ApiUtils.success("성공"));
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

        if ("edu".equals(paymentReqDTO.getStatusType())) {
            paymentReqDTO.setEduStatus("issued");
        }
        if ("material".equals(paymentReqDTO.getStatusType())) {
            paymentReqDTO.setMaterialStatus("issued");
        }

        paymentReqDTO.setUserCode(user.getUserCode());
        paymentReqDTO.setCenterCode(user.getCenterCode());

        paymentService.insertPayment(paymentReqDTO);

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/edu-personal")
    public ResponseEntity<?> getPersonalModal(@RequestBody Map<String, String> studentId) {


        System.out.println("모달 데이터 조회");
        System.out.println("student_id = " + studentId.get("studentId"));
        List<PaymentRespDTO.PaymentDetailDTO> response = paymentService.findPaymentByStudentId(studentId.get("studentId"));
        System.out.println("==========================================");
        System.out.println(response);
        System.out.println("==========================================");
        return ResponseEntity.ok(ApiUtils.success(response));
    }

}
