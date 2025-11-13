package com.hohoedu.all_pass.payment;

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
    public ResponseEntity<String> sendBill(@RequestBody Map<String, Object> body) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        String url = "https://stg.paymint.co.kr/partner/if/bill/send";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }


    // 결제선생 청구서 저장
    @PostMapping("/bill/insert")
    public ResponseEntity<?> createBill(@RequestBody PaymentReqDTO.InsertBillDTO reqDTO, HttpSession session) throws Exception {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        // 2) 요청값 validation
        if (reqDTO == null) {
            return ResponseEntity.badRequest()
                    .body(ApiUtils.error("요청 데이터가 비어 있습니다.", HttpStatus.BAD_REQUEST));
        }

        if (reqDTO.getBillId() == null || reqDTO.getBillId().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiUtils.error("bill_id가 누락되었습니다.", HttpStatus.BAD_REQUEST));
        }

        if (reqDTO.getPaymentKey() == null || reqDTO.getPaymentKey().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiUtils.error("paymentKey가 없습니다.", HttpStatus.BAD_REQUEST));
        }

        reqDTO.setCenterCode(user.getCenterCode());

        paymentService.insertPaymentBill(reqDTO, user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success("청구서 생성 완료"));
    }

    // 결제선생 콜백
    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody PaymentReqDTO.PayCallbackDTO dto) {

        paymentService.insertPaymentCallback(dto);

        return ResponseEntity.ok(ApiUtils.success("성공"));
    }

    // 데이터 필터링
    @PostMapping("/students")
    public ResponseEntity<?> getStudents(HttpSession session, @RequestBody PaymentReqDTO.StudentsByMonthDTO paymentReqDTO) {

        String year = paymentReqDTO.getYear();
        String month = paymentReqDTO.getMonth();
        String userCode = paymentReqDTO.getUserCode();

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentService.findByAssignStudent(year, month, userCode);

        return ResponseEntity.ok(ApiUtils.success(students));
    }


    @PostMapping("edu-personal")
    public ResponseEntity<?> getPersonalModal(@RequestBody Map<String, String> studentId) {

        List<PaymentRespDTO.PaymentModalDTO> response = paymentService.findPaymentByStudentId(studentId.get("studentId"));

        return ResponseEntity.ok(ApiUtils.success(response));
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
