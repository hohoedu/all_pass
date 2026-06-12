package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LogisticsRepository {

    List<LogisRespDTO.ReorderListDTO> findReorderList(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode,
            @Param("onlyWait") boolean onlyWait);
}
