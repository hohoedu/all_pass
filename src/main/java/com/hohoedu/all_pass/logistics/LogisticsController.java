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

    @PostMapping("/order/confirmed")
    public ResponseEntity<?> updateConfirmed(@RequestBody LogisReqDTO.ConfirmedUpdateReqDTO req) {
        logisticsService.updateConfirmed(req);
        return ResponseEntity.ok(ApiUtils.success(null));
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
        List<LogisRespDTO.CenterAggregateDTO> result = "센터별 선생님 집계".equals(req.getSegmentType())
                ? logisticsService.findTeacherAggregate(req)
                : logisticsService.findCenterAggregate(req);

        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/order/manual/save")
    public ResponseEntity<?> saveOrderManual(@RequestBody LogisReqDTO.OrderManualSaveReqDTO dto) {
        String manualId = logisticsService.saveOrderManual(dto);
        return ResponseEntity.ok(ApiUtils.success(manualId));
    }

    @PostMapping("/order/manual/list")
    public ResponseEntity<?> getManualList(@RequestBody LogisReqDTO.ManualListReqDTO dto) {

        return ResponseEntity.ok(ApiUtils.success(logisticsService.getManualList(dto)));
    }

    @PostMapping("/order/manual/items")
    public ResponseEntity<?> getManualItems(@RequestBody LogisReqDTO.ManualItemsReqDTO dto) {
        return ResponseEntity.ok(ApiUtils.success(logisticsService.getManualItems(dto)));
    }

    @PostMapping("/order/manual-add/options")
    public ResponseEntity<?> getManualAddOptions(@RequestBody LogisReqDTO.ManualAddOptionsReqDTO req) {
        return ResponseEntity.ok(ApiUtils.success(logisticsService.getManualAddOptions(req)));
    }

    @PostMapping("/order/reorder/manual-add")
    public ResponseEntity<?> addReorderManually(@RequestBody LogisReqDTO.ReorderManualAddReqDTO req) {
        logisticsService.addReorderManually(req);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/order/manual/items/add")
    public ResponseEntity<?> addManualItems(@RequestBody LogisReqDTO.ManualItemAddReqDTO req) {
        logisticsService.addManualItems(req);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/order/unit-codes-by-class")
    public ResponseEntity<?> getUnitCodesByClass(@RequestBody LogisReqDTO.UnitCodeByClassKeyReqDTO req) {
        return ResponseEntity.ok(ApiUtils.success(logisticsService.getUnitCodesByClassKey(req.getClassKey())));
    }

    @PostMapping("/order/manual/delete")
    public ResponseEntity<?> deleteManual(@RequestBody LogisReqDTO.ManualItemsReqDTO req) {
        logisticsService.deleteManual(req.getManualId());
        return ResponseEntity.ok(ApiUtils.success(null));
    }


}
