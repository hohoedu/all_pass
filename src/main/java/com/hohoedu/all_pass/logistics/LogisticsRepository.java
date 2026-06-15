package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LogisticsRepository {

    List<LogisRespDTO.DeadlineDTO> findAllDeadlines();

    void updateDeadline(String centerCode, int deadlineAt);

    List<LogisRespDTO.SelectCenterDTO.ReorderListDTO> findReorderList(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode,
            @Param("onlyWait") boolean onlyWait);

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
}
