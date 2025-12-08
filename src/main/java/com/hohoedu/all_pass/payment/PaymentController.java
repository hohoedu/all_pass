package com.hohoedu.all_pass.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제선생 청구서 발행
    @PostMapping("/send")
    public ResponseEntity<?> sendBill(HttpSession session, @RequestBody PaymentReqDTO.PaySendReqDTO dto) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        PaymentRespDTO.PaySendRespDTO res = paymentService.sendBill(user, dto);

        if (!"0000".equals(res.getPaymintCode())) {
            return ResponseEntity.ok(ApiUtils.error("청구서 발행 실패: " + res.getPaymintMsg(), HttpStatus.INTERNAL_SERVER_ERROR));
        }

        if (!res.isDbSaved()) {
            return ResponseEntity.ok(ApiUtils.error("청구는 되었지만 ERP 저장에 실패했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR));
        }

        return ResponseEntity.ok(ApiUtils.success("청구서 발행 완료"));
    }

    // 결제선생 청구서 저장
//    @PostMapping("/bill/insert")
//    public ResponseEntity<?> createBill(@RequestBody PaymentReqDTO.InsertBillDTO reqDTO, HttpSession session) throws Exception {
//        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header(HttpHeaders.LOCATION, "/login")
//                    .build();
//        }
//        log.info("청구서 발행 후 저장");
//
//        // 2) 요청값 validation
//        if (reqDTO == null) {
//            return ResponseEntity.badRequest()
//                    .body(ApiUtils.error("요청 데이터가 비어 있습니다.", HttpStatus.BAD_REQUEST));
//        }
//
//        if (reqDTO.getBillId() == null || reqDTO.getBillId().isEmpty()) {
//            return ResponseEntity.badRequest()
//                    .body(ApiUtils.error("bill_id가 누락되었습니다.", HttpStatus.BAD_REQUEST));
//        }
//
//        if (reqDTO.getPaymentKey() == null || reqDTO.getPaymentKey().isEmpty()) {
//            return ResponseEntity.badRequest()
//                    .body(ApiUtils.error("paymentKey가 없습니다.", HttpStatus.BAD_REQUEST));
//        }
//
//        reqDTO.setCenterCode(user.getCenterCode());
//
//        paymentService.insertPaymentBill(reqDTO, user.getUserCode());
//
//        return ResponseEntity.ok(ApiUtils.success("청구서 생성 완료"));
//    }

    // 결제선생 콜백
    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody PaymentReqDTO.PayCallbackDTO dto) {

        paymentService.insertPaymentCallback(dto);

        return ResponseEntity.ok(ApiUtils.success("성공"));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manualPay(HttpSession session, @RequestBody PaymentReqDTO.ManualPaymentReqDTO dto) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        try {
            dto.setCenterCode(user.getCenterCode());
            PaymentRespDTO.ManualPaymentRespDTO response = paymentService.processManualPayment(dto);
            return ResponseEntity.ok(ApiUtils.success(response));
        } catch (Exception e) {
            log.error("수기 결제 실패", e);
            return ResponseEntity.ok(ApiUtils.error("수기 결제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/destroy/bill")
    public ResponseEntity<?> destroyBills(HttpSession session, @RequestBody PaymentReqDTO.PayDestroyReqDTO dto) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        paymentService.destroyBill(user, dto);

        return ResponseEntity.ok(ApiUtils.success("청구서 파기 완료"));
    }


    // 데이터 필터링
    @PostMapping("/students")
    public ResponseEntity<?> getStudents(HttpSession session, @RequestBody PaymentReqDTO.StudentsByMonthDTO paymentReqDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        String year = paymentReqDTO.getYear();
        String month = paymentReqDTO.getMonth();
        String userCode = paymentReqDTO.getUserCode();

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentService.findByAssignStudent(year, month, userCode, user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(students));
    }


    @PostMapping("edu-personal")
    public ResponseEntity<?> getPersonalModal(HttpSession session, @RequestBody Map<String, String> studentId) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<PaymentRespDTO.PaymentModalDTO> response = paymentService.findPaymentByStudentId(studentId.get("studentId"), user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/list/students")
    public ResponseEntity<?> getListStudents(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<PaymentRespDTO.UnpaidStudentDTO> studentList = paymentService.findUnpaidStudent(user.getCenterCode(), user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(studentList));

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


}
