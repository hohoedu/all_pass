package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryLogisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsRepository logisticsRepository;
    private final SecondaryLogisticsRepository secondaryLogisticsRepository;
    private static final Set<String> SECONDARY_CENTERS = Set.of("ULS001");

    public List<LogisRespDTO.DeadlineDTO> findAllDeadlines() {
        return logisticsRepository.findAllDeadlines();
    }

    public void updateDeadline(LogisReqDTO.DeadlineUpdateReqDTO req) {
        logisticsRepository.updateDeadline(req.getCenterCode(), req.getDeadlineAt());
    }

    public Map<String, LogisRespDTO.ReorderStatusDTO> findReorderStatusMap(String year, String month) {
        Map<String, LogisRespDTO.ReorderStatusDTO> result = logisticsRepository.findReorderStatusByMonth(year, month)
                .stream()
                .collect(Collectors.toMap(
                        LogisRespDTO.ReorderStatusDTO::getCenterCode,
                        Function.identity()
                ));

        List<LogisRespDTO.ReorderStatusDTO> secondaryStatus =
                secondaryLogisticsRepository.findReorderStatusByMonth(year, month);

        secondaryStatus.forEach(s -> result.put(s.getCenterCode(), s));

        return result;
    }

    public LogisRespDTO.SelectCenterDTO findCenterOrderData(LogisReqDTO.ReorderListReqDTO req) {
        boolean isSecondary = SECONDARY_CENTERS.contains(req.getCenterCode());

        List<LogisRespDTO.SelectCenterDTO.ReorderListDTO> reorderList = isSecondary
                ? secondaryLogisticsRepository.findReorderList(req.getYear(), req.getMonth(), req.getCenterCode(), req.isOnlyWait())
                : logisticsRepository.findReorderList(req.getYear(), req.getMonth(), req.getCenterCode(), req.isOnlyWait());

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

    public void updateConfirmed(LogisReqDTO.ConfirmedUpdateReqDTO req) {
        boolean isSecondary = SECONDARY_CENTERS.contains(req.getCenterCode());

        if (isSecondary) {
            String schk = switch (req.getConfirmed()) {
                case "checked" -> "Y";
                case "unchecked" -> "N";
                case "user_cancel" -> "C";
                case "acancel" -> "D";
                default -> throw new IllegalArgumentException("invalid confirmed value");
            };
            secondaryLogisticsRepository.updateConfirmed(req.getId(), schk);
        } else {
            logisticsRepository.updateConfirmed(req.getId(), req.getConfirmed());
        }
    }

    public List<LogisRespDTO.InvoiceDTO> findInvoice(LogisReqDTO.ReorderListReqDTO req) {
        boolean isSecondary = SECONDARY_CENTERS.contains(req.getCenterCode());

        return isSecondary
                ? secondaryLogisticsRepository.findInvoice(req.getYear(), req.getMonth())
                : logisticsRepository.findInvoiceByCenterCode(req.getYear(), req.getMonth(), req.getCenterCode());
    }

    public List<LogisRespDTO.CenterAggregateDTO> findCenterAggregate(LogisReqDTO.AggregateReqDTO req) {
        List<LogisRespDTO.CenterAggregateDTO> result = new ArrayList<>();

        if (req.getCenterCodes().contains("all")) {
            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> primaryItems =
                    logisticsRepository.findAggregateItemsAll(req.getYear(), req.getMonth());

            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> secondaryItems =
                    secondaryLogisticsRepository.findAggregateItemsByCenterCode(req.getYear(), req.getMonth(), "ULS001");

            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> mergedItems =
                    mergeAggregateItems(primaryItems, secondaryItems);

            LogisRespDTO.CenterAggregateDTO dto = new LogisRespDTO.CenterAggregateDTO();
            dto.setCenterCode("all");
            dto.setCenterName("전체");
            dto.setItems(mergedItems);
            result.add(dto);
            return result;
        }

        for (String centerCode : req.getCenterCodes()) {
            boolean isSecondary = SECONDARY_CENTERS.contains(centerCode);

            String centerName = logisticsRepository.findCenterNameByCode(centerCode);

            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> items = isSecondary
                    ? secondaryLogisticsRepository.findAggregateItemsByCenterCode(req.getYear(), req.getMonth(), centerCode)
                    : logisticsRepository.findAggregateItemsByCenterCode(req.getYear(), req.getMonth(), centerCode);

            LogisRespDTO.CenterAggregateDTO dto = new LogisRespDTO.CenterAggregateDTO();
            dto.setCenterCode(centerCode);
            dto.setCenterName(centerName);
            dto.setItems(items);
            result.add(dto);
        }

        return result;
    }

    public List<LogisRespDTO.CenterAggregateDTO> findTeacherAggregate(LogisReqDTO.AggregateReqDTO req) {
        List<LogisRespDTO.CenterAggregateDTO> result = new ArrayList<>();

        for (String centerCode : req.getCenterCodes()) {
            boolean isSecondary = SECONDARY_CENTERS.contains(centerCode);
            String centerName = logisticsRepository.findCenterNameByCode(centerCode);

            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> items = isSecondary
                    ? secondaryLogisticsRepository.findAggregateItemsByTeacherGroup(req.getYear(), req.getMonth(), centerCode)
                    : logisticsRepository.findAggregateItemsByTeacherGroup(req.getYear(), req.getMonth(), centerCode);

            LogisRespDTO.CenterAggregateDTO dto = new LogisRespDTO.CenterAggregateDTO();
            dto.setCenterCode(centerCode);
            dto.setCenterName(centerName);
            dto.setItems(items);
            result.add(dto);
        }

        return result;
    }

    private List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> mergeAggregateItems(
            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> a,
            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> b) {

        Map<String, LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> merged = new LinkedHashMap<>();

        for (var item : a) {
            merged.put(item.getClassName() + "|" + item.getUnitName(), item);
        }

        for (var item : b) {
            String key = item.getClassName() + "|" + item.getUnitName();
            var existing = merged.get(key);
            if (existing != null) {
                existing.setBaseCount(existing.getBaseCount() + item.getBaseCount());
                existing.setTeacherCount(existing.getTeacherCount() + item.getTeacherCount());
                existing.setReorderCount(existing.getReorderCount() + item.getReorderCount());
                existing.setTotalCount(existing.getTotalCount() + item.getTotalCount());
                existing.setTimeTableCount(existing.getTimeTableCount() + item.getTimeTableCount());
            } else {
                merged.put(key, item);
            }
        }

        return new ArrayList<>(merged.values());
    }

    public LogisRespDTO.SelectCenterDTO.CenterInfoDTO findCenterInfo(String centerCode) {
        return logisticsRepository.findCenterInfoByCenterCode(centerCode);
    }
}
