package com.hohoedu.all_pass.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
import com.google.protobuf.Api;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass._core.utils.SseEmitterHolder;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SseEmitterHolder sseEmitterHolder;
    private final StudentService studentService;

    @GetMapping("/access-url")
    public ResponseEntity<?> getAccessURL(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {

//            String url = paymentService.getPaymintAccessURL(user.getCenterCode());
            String url = "https://manager.payssam.kr/";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, url)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping(value = "/progress/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter progress(@PathVariable String jobId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5분 타임아웃
        sseEmitterHolder.add(jobId, emitter);

        emitter.onCompletion(() -> sseEmitterHolder.remove(jobId));
        emitter.onTimeout(() -> sseEmitterHolder.remove(jobId));
        emitter.onError(e -> sseEmitterHolder.remove(jobId));

        return emitter;
    }

    // 결제선생 청구서 발행
    @PostMapping("/send")
    public ResponseEntity<?> sendBill(HttpSession session, @RequestBody PaymentReqDTO.PaySendReqDTO dto) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        Map<String, Integer> result = paymentService.sendBill(user, dto);

        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueBill(HttpSession session, @RequestBody PaymentReqDTO.PayReissueReqDTO dto) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {
            paymentService.reissueBill(user, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "msg", "재발행이 완료되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "msg", e.getMessage()
            ));
        }
    }

    // 결제선생 콜백
    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody PaymentReqDTO.PayCallbackDTO dto) {

        paymentService.insertPaymentCallback(dto);
        paymentService.callbackProcess(dto);

        return ResponseEntity.ok(Map.of(
                "code", "0000",
                "msg", "성공하였습니다."
        ));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manualPay(HttpSession session, @RequestBody PaymentReqDTO.ManualPaymentReqDTO dto) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        dto.setUserCode(user.getUserCode());
        dto.setCenterCode(user.getCenterCode());

        log.info(dto.toString());
//        PaymentRespDTO.ManualPaymentRespDTO response = paymentService.insertPaymentManual(dto);
        String response = paymentService.insertPaymentManual(dto);
        return ResponseEntity.ok(ApiUtils.success(response));
//        return ResponseEntity.ok(ApiUtils.success(null));

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
        String userCode = user.getRoleKey().equals("ADMIN") || user.getRoleKey().equals("MANAGER") ? paymentReqDTO.getUserCode() : user.getUserCode();

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentService.findByAssignStudent(year, month, userCode, user.getCenterCode(), paymentReqDTO.getItemType());
        log.info("students = {}", students);
        return ResponseEntity.ok(ApiUtils.success(students));
    }


    @PostMapping("/edu-personal")
    public ResponseEntity<?> getPersonalModal(HttpSession session, @RequestBody PaymentReqDTO.PersonalDTO reqDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        reqDTO.setCenterCode(user.getCenterCode());

        List<PaymentRespDTO.PaymentModalDTO> response = paymentService.findPaymentByStudentId(reqDTO);

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/detail/bill")
    public ResponseEntity<?> getDetailPaymnetBill(HttpSession session, @RequestBody PaymentReqDTO.PersonalDTO reqDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        List<PaymentRespDTO.DetailPaymentBillDTO> response = paymentService.findPaymentDetailsByStudentId(reqDTO);
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/list/students")
    public ResponseEntity<?> getListStudents(HttpSession session, @RequestBody PaymentReqDTO.StudentsByMonthDTO reqDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<PaymentRespDTO.UnpaidStudentDTO> studentList = paymentService.findUnpaidStudent(user.getCenterCode(), user.getUserCode(), reqDTO.getYear(), reqDTO.getMonth());

        return ResponseEntity.ok(ApiUtils.success(studentList));

    }

    @PostMapping("/find/bill")
    public ResponseEntity<?> getBill(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }


        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(HttpSession session, @RequestBody PaymentReqDTO.PaymentCancelReqDTO reqDTO) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        log.info(reqDTO.toString());
        paymentService.cancelPayment(user, reqDTO);

        return ResponseEntity.ok(ApiUtils.success(null));
    }


    @PostMapping("/refund")
    public ResponseEntity<?> paymentRefund() {
        paymentService.insertPaymentRefund();
        return ResponseEntity.ok(ApiUtils.success(null));
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

    @PostMapping("/update-fee")
    public ResponseEntity<?> updateEduPrice(HttpSession session, @RequestBody PaymentReqDTO.EduFeeUpdateReqDTO reqDTO) {

        paymentService.updateEduFeeAndRecalculate(reqDTO);


        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/api/cashbill/students")
    public ResponseEntity<?> getCashPaymentStudents(HttpSession session, @RequestBody PaymentReqDTO.CashbillStudentReqDTO reqDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<PaymentRespDTO.CashbillStudentRespDTO> students = paymentService.getCashPaymentStudents(
                reqDTO.getYear(), reqDTO.getMonth(), user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(students));

    }

    @PostMapping("/api/cashbill/issue")
    public ResponseEntity<?> cashbillIssue(HttpSession session, @RequestBody PaymentReqDTO.CashbillIssueReqDTO reqDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        try {
            paymentService.issueCashbill(user, reqDTO);
            return ResponseEntity.ok(ApiUtils.success(null));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }


    @PostMapping("/api/claim/export")
    public ResponseEntity<List<PaymentRespDTO.ClaimDto>> exportClaims(@RequestBody PaymentReqDTO.ClaimFilterDTO filters) {
        List<PaymentRespDTO.ClaimDto> data = paymentService.getFilteredClaims(filters);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/cashbill/history")
    public ResponseEntity<?> getCashbillHistory(@RequestBody PaymentReqDTO.YearMonthDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<PaymentRespDTO.CashBillHistoryDTO> response = paymentService.getCashbillHistory(user.getCenterCode(), dto.getYear(), dto.getMonth());
        log.info(response.toString());
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/api/cashbill/cancel")
    public ResponseEntity<?> cancelCashbills(@RequestBody PaymentReqDTO.CashbillCancelDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        log.info("현금영수증 취소 요청: billIds = {}", dto.getBillId());

        try {
            PaymentRespDTO.CashbillCancelRespDTO result = paymentService.cancelCashbills(dto.getBillId(), dto.getReason(), user.getCenterCode());
            return ResponseEntity.ok(ApiUtils.success(result));

        } catch (Exception e) {
            log.error("현금영수증 취소 중 오류 발생", e);
            return ResponseEntity.ok(ApiUtils.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/api/remind/unpaid-students")
    public ResponseEntity<?> findUnpaidStudents(@RequestBody PaymentReqDTO.UnpaidStudentReqDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        dto.setCenterCode(user.getCenterCode());
        List<PaymentRespDTO.UnpaidStudentRespDTO> response = paymentService.findUnpaidStudentForAlimtalk(dto);
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/list/payments")
    public ResponseEntity<?> getPayments(@RequestBody Map<String, String> body, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String year = body.get("year");
        String month = body.get("month");

        List<PaymentRespDTO.MonthlyPaymentDTO> payments = paymentService.findMonthlyPayments(
                user.getUserCode(),
                user.getCenterCode(),
                year,
                month
        );

        return ResponseEntity.ok(Map.of("response", payments));
    }

    @PostMapping("/manual/update/{id}")
    public ResponseEntity<?> updatePayment(@PathVariable Integer id, @RequestBody PaymentReqDTO.UpdatePaymentDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        paymentService.updatePayment(id, dto, user.getCenterCode());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "수정되었습니다.");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/manual/delete/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Integer id, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        paymentService.deletePayment(id, user.getCenterCode());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "삭제되었습니다.");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/update-phone")
    @ResponseBody
    public ResponseEntity<?> updatePhone(@RequestBody PaymentReqDTO.UpdateBillingPhone request) {
        try {

            log.info("requestDTO = {}", request.toString());
            studentService.updateStudentBillingPhone(request.getStudentId(), request.getPhone());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "msg", e.getMessage()));
        }
    }

}
