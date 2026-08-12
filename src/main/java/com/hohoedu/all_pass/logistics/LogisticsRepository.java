package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LogisticsRepository {

        List<LogisRespDTO.DeadlineDTO> findAllDeadlines();

        void updateDeadline(
                        @Param("centerCode") String centerCode,
                        @Param("deadlineAt") int deadlineAt);

        List<LogisRespDTO.ReorderStatusDTO> findReorderStatusByMonth(
                        @Param("year") String year,
                        @Param("month") String month);

        List<LogisRespDTO.SelectCenterDTO.ReorderListDTO> findReorderList(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode,
                        @Param("onlyWait") boolean onlyWait);

        void updateConfirmed(
                        @Param("id") String id,
                        @Param("confirmed") String confirmed);

        List<LogisRespDTO.SelectCenterDTO.SummaryInvoiceDTO> findSummaryInvoiceByCenterCode(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        LogisRespDTO.SelectCenterDTO.CenterInfoDTO findCenterInfoByCenterCode(
                        @Param("centerCode") String centerCode);

        List<LogisRespDTO.InvoiceDTO> findInvoiceByCenterCode(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        String findCenterNameByCode(String centerCode);

        List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> findAggregateItemsAll(
                        @Param("year") String year,
                        @Param("month") String month);

        List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> findAggregateItemsByCenterCode(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> findAggregateItemsByTeacherGroup(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        void insertOrderManual(
                        @Param("dto") LogisReqDTO.OrderManualSaveReqDTO dto,
                        @Param("manualId") String manualId);

        void insertOrderManualItem(
                        @Param("manualId") String manualId,
                        @Param("item") LogisReqDTO.OrderManualSaveReqDTO.OrderManualItemDTO item);

        List<Map<String, Object>> selectManualList(LogisReqDTO.ManualListReqDTO dto);

        List<Map<String, Object>> selectManualItems(LogisReqDTO.ManualItemsReqDTO dto);

        List<Map<String, Object>> selectClassCodes();

        List<Map<String, Object>> selectUnitCodesByClassKey(@Param("classKey") String classKey);

        void insertReorderManualAdd(
                        @Param("item") LogisReqDTO.ReorderManualAddReqDTO.ReorderAddItemDTO item,
                        @Param("centerCode") String centerCode,
                        @Param("year") String year,
                        @Param("month") String month);

        Map<String, Object> selectManualHeaderByManualId(LogisReqDTO.ManualItemsReqDTO dto);

        void deleteManualItemsByManualId(@Param("manualId") String manualId);

        void deleteManualByManualId(@Param("manualId") String manualId);

        void updateManualTotalByManualId(@Param("manualId") String manualId);

        List<LogisRespDTO.ReorderSugiItemDTO> selectReorderSugiItems(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        void deleteReorderSugiByCenterMonth(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        List<LogisRespDTO.OrderHistoryDTO> findTeacherOrderHistory(
                        @Param("centerCode") String centerCode,
                        @Param("userCode") String userCode,
                        @Param("year") String year,
                        @Param("month") String month);
}
