package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryLogisticsRepository;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsRepository logisticsRepository;
    private final SecondaryLogisticsRepository secondaryLogisticsRepository;
    private static final Set<String> SECONDARY_CENTERS = Set.of("ULS001");
    private final UserRepository userRepository;

    public List<LogisRespDTO.DeadlineDTO> findAllDeadlines() {
        List<LogisRespDTO.DeadlineDTO> result = new ArrayList<>(logisticsRepository.findAllDeadlines());

        Integer secondaryDeadline = secondaryLogisticsRepository.findOrderDeadline();
        if (secondaryDeadline != null) {
            result.removeIf(d -> "ULS001".equals(d.getCenterCode()));

            LogisRespDTO.DeadlineDTO dto = new LogisRespDTO.DeadlineDTO();
            dto.setCenterCode("ULS001");
            dto.setDeadlineAt(secondaryDeadline);
            result.add(dto);
        }

        return result;
    }

    public void updateDeadline(LogisReqDTO.DeadlineUpdateReqDTO req) {
        logisticsRepository.updateDeadline(req.getCenterCode(), req.getDeadlineAt());

        if (SECONDARY_CENTERS.contains(req.getCenterCode())) {
            secondaryLogisticsRepository.updateOrderDeadline(req.getDeadlineAt());
        }
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

    /**
     * 올패스(primary) class_key 순서. secondaryLogistics.xml의 findAggregateItemsByCenterCode에서
     * 유곡 ggubun 코드를 이 class_key 값으로 치환해서 내려주므로, 두 DB 결과가 같은 척도로 정렬된다.
     * logistics.xml의 findAggregateItemsAll ORDER BY에 있던 CASE 목록과 동일하다.
     */
    private static final List<String> AGGREGATE_CLASS_KEY_ORDER = List.of(
            "Y", "S", "P", "ES", "EP", "EG", "ED", "HSN", "HSU", "HSX", "HSS", "HSA",
            "K", "M", "J", "SU", "DU", "TU", "BSN", "BSU", "BSX", "BSS");

    private static final Pattern UNIT_NUMBER_PATTERN = Pattern.compile("\\d+");

    private List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> mergeAggregateItems(
            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> a,
            List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> b) {

        List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> merged = new ArrayList<>(a);
        Map<String, LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> index = new HashMap<>();
        for (var item : merged) {
            index.put(aggregateKey(item), item);
        }

        for (var item : b) {
            String key = aggregateKey(item);
            var existing = index.get(key);
            if (existing != null) {
                existing.setBaseCount(existing.getBaseCount() + item.getBaseCount());
                existing.setTeacherCount(existing.getTeacherCount() + item.getTeacherCount());
                existing.setReorderCount(existing.getReorderCount() + item.getReorderCount());
                existing.setTotalCount(existing.getTotalCount() + item.getTotalCount());
                existing.setTimeTableCount(existing.getTimeTableCount() + item.getTimeTableCount());
            } else {
                merged.add(item);
                index.put(key, item);
            }
        }

        merged.sort(Comparator
                .comparingInt((LogisRespDTO.CenterAggregateDTO.AggregateItemDTO item) -> classKeyOrder(item.getClassKey()))
                .thenComparingInt(item -> extractUnitNumber(item.getUnitName()))
                .thenComparing(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO::getUnitName));

        return merged;
    }

    private String aggregateKey(LogisRespDTO.CenterAggregateDTO.AggregateItemDTO item) {
        return item.getClassName() + "|" + item.getUnitName();
    }

    private int classKeyOrder(String classKey) {
        if (classKey == null) {
            return Integer.MAX_VALUE;
        }
        int idx = AGGREGATE_CLASS_KEY_ORDER.indexOf(classKey);
        return idx >= 0 ? idx : Integer.MAX_VALUE;
    }

    private int extractUnitNumber(String unitName) {
        if (unitName == null) {
            return Integer.MAX_VALUE;
        }
        Matcher matcher = UNIT_NUMBER_PATTERN.matcher(unitName);
        return matcher.find() ? Integer.parseInt(matcher.group()) : Integer.MAX_VALUE;
    }

    public LogisRespDTO.SelectCenterDTO.CenterInfoDTO findCenterInfo(String centerCode) {
        return logisticsRepository.findCenterInfoByCenterCode(centerCode);
    }

    @Transactional
    public String saveOrderManual(LogisReqDTO.OrderManualSaveReqDTO dto) {
        String manualId = generateManualId();

        logisticsRepository.insertOrderManual(dto, manualId);

        if (dto.getItems() != null) {
            for (LogisReqDTO.OrderManualSaveReqDTO.OrderManualItemDTO item : dto.getItems()) {
                logisticsRepository.insertOrderManualItem(manualId, item);
            }
        }

        return manualId;
    }

    private String generateManualId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    public List<Map<String, Object>> getManualList(LogisReqDTO.ManualListReqDTO dto) {
        return logisticsRepository.selectManualList(dto);
    }

    public List<Map<String, Object>> getManualItems(LogisReqDTO.ManualItemsReqDTO dto) {
        return logisticsRepository.selectManualItems(dto);
    }

    public Map<String, Object> getManualAddOptions(LogisReqDTO.ManualAddOptionsReqDTO req) {
        Map<String, Object> result = new HashMap<>();

        List<User> users = userRepository.findAllUserCode(req.getCenterCode()).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsHan()) || Boolean.TRUE.equals(u.getIsBook()))
                .collect(Collectors.toList());

        result.put("classCodes", logisticsRepository.selectClassCodes());
        result.put("users", users);
        return result;
    }

    public List<Map<String, Object>> getUnitCodesByClassKey(String classKey) {
        return logisticsRepository.selectUnitCodesByClassKey(classKey);
    }

    @Transactional
    public void addReorderManually(LogisReqDTO.ReorderManualAddReqDTO req) {
        for (LogisReqDTO.ReorderManualAddReqDTO.ReorderAddItemDTO item : req.getItems()) {
            logisticsRepository.insertReorderManualAdd(item, req.getCenterCode(), req.getYear(), req.getMonth());
        }
    }

    @Transactional
    public void addManualItems(LogisReqDTO.ManualItemAddReqDTO req) {
        for (LogisReqDTO.OrderManualSaveReqDTO.OrderManualItemDTO item : req.getItems()) {
            logisticsRepository.insertOrderManualItem(req.getManualId(), item);
        }
    }

    public Map<String, Object> getManualHeader(LogisReqDTO.ManualItemsReqDTO dto) {
        return logisticsRepository.selectManualHeaderByManualId(dto);
    }


    @Transactional
    public void deleteManual(String manualId) {
        logisticsRepository.deleteManualItemsByManualId(manualId);
        logisticsRepository.deleteManualByManualId(manualId);
    }

    // ── 수기 ──
    @Transactional
    public void replaceManualItems(LogisReqDTO.ManualItemAddReqDTO req) {
        logisticsRepository.deleteManualItemsByManualId(req.getManualId());

        if (req.getItems() != null) {
            for (LogisReqDTO.OrderManualSaveReqDTO.OrderManualItemDTO item : req.getItems()) {
                logisticsRepository.insertOrderManualItem(req.getManualId(), item);
            }
        }

        logisticsRepository.updateManualTotalByManualId(req.getManualId());
    }

    // ── 정기 로드 ──
    public List<LogisRespDTO.ReorderSugiItemDTO> getReorderSugiItems(LogisReqDTO.ReorderManualAddReqDTO req) {
        return logisticsRepository.selectReorderSugiItems(req.getYear(), req.getMonth(), req.getCenterCode());
    }

    // ── 정기 교체 ──
    @Transactional
    public void replaceReorderManualAdd(LogisReqDTO.ReorderManualAddReqDTO req) {
        logisticsRepository.deleteReorderSugiByCenterMonth(req.getYear(), req.getMonth(), req.getCenterCode());

        if (req.getItems() != null) {
            for (LogisReqDTO.ReorderManualAddReqDTO.ReorderAddItemDTO item : req.getItems()) {
                logisticsRepository.insertReorderManualAdd(item, req.getCenterCode(), req.getYear(), req.getMonth());
            }
        }
    }

    // ── 선생님별 회차별 주문 내역 ──
    public List<LogisRespDTO.OrderHistoryDTO> getTeacherOrderHistory(LogisReqDTO.TeacherOrderHistoryReqDTO req) {
        return logisticsRepository.findTeacherOrderHistory(req.getCenterCode(), req.getUserCode(), req.getYear(),
                req.getMonth());
    }
}
