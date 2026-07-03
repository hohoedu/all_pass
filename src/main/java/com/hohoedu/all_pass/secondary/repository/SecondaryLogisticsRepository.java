package com.hohoedu.all_pass.secondary.repository;

import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SecondaryLogisticsRepository {

        List<LogisRespDTO.InvoiceDTO> findInvoice(
                        @Param("year") String year,
                        @Param("month") String month);

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

        List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> findAggregateItemsByCenterCode(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        List<LogisRespDTO.CenterAggregateDTO.AggregateItemDTO> findAggregateItemsByTeacherGroup(
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("centerCode") String centerCode);

        void updateOrderDeadline(@Param("deadline") Integer deadline);

        Integer findOrderDeadline();

}
