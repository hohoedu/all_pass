package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsRepository logisticsRepository;

    public List<LogisRespDTO.DeadlineDTO> findAllDeadlines() {
        return logisticsRepository.findAllDeadlines();
    }

    public void updateDeadline(LogisReqDTO.DeadlineUpdateReqDTO req) {
        logisticsRepository.updateDeadline(req.getCenterCode(), req.getDeadlineAt());
    }

    public LogisRespDTO.SelectCenterDTO findCenterOrderData(LogisReqDTO.ReorderListReqDTO req) {
        List<LogisRespDTO.SelectCenterDTO.ReorderListDTO> reorderList =
                logisticsRepository.findReorderList(req.getYear(), req.getMonth(), req.getCenterCode(), req.isOnlyWait());

        List<LogisRespDTO.SelectCenterDTO.SummaryInvoiceDTO> summaryInvoice =
                logisticsRepository.findSummaryInvoiceByCenterCode(req.getYear(), req.getMonth(), req.getCenterCode());

        LogisRespDTO.SelectCenterDTO.CenterInfoDTO centerInfo =
                logisticsRepository.findCenterInfoByCenterCode(req.getCenterCode());

        LogisRespDTO.SelectCenterDTO result = new LogisRespDTO.SelectCenterDTO();
        result.setReorderList(reorderList);
        result.setSummaryInvoice(summaryInvoice);
        result.setCenterInfo(centerInfo);
        return result;
    }

    public List<LogisRespDTO.InvoiceDTO> findInvoice(LogisReqDTO.ReorderListReqDTO req) {
        return logisticsRepository.findInvoiceByCenterCode(req.getYear(), req.getMonth(), req.getCenterCode());
    }
}
