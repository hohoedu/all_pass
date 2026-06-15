package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/logis")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/order/reorder-list")
    public ResponseEntity<?> getReorderList(@RequestBody LogisReqDTO.ReorderListReqDTO req) {
        log.info("getReorderList");
        LogisRespDTO.SelectCenterDTO list = logisticsService.findCenterOrderData(req);
        return ResponseEntity.ok(ApiUtils.success(list));
    }

    @PostMapping("/order/invoice")
    public ResponseEntity<?> getInvoice(@RequestBody LogisReqDTO.ReorderListReqDTO req) {
        List<LogisRespDTO.InvoiceDTO> list = logisticsService.findInvoice(req);
        return ResponseEntity.ok(ApiUtils.success(list));
    }

    @PostMapping("/order/deadline")
    public ResponseEntity<?> updateDeadline(@RequestBody LogisReqDTO.DeadlineUpdateReqDTO req) {
        logisticsService.updateDeadline(req);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/order/aggregate")
    public ResponseEntity<?> getAggregate(@RequestBody LogisReqDTO.AggregateReqDTO req) {
        log.info("getAggregate - segmentType: {}, centers: {}", req.getSegmentType(), req.getCenterCodes());
        // 서비스 연결은 다음 단계
        return ResponseEntity.ok(ApiUtils.success(null));
    }

}
