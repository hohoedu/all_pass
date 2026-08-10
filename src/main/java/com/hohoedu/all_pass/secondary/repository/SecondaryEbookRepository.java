package com.hohoedu.all_pass.secondary.repository;

import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SecondaryEbookRepository {

    List<SecondaryDTO.EbookYearConfigDTO> findEbookYearConfig(@Param("year") String year);

    List<SecondaryDTO.KeycodeRawDTO> findKeycodeList(
            @Param("year") String year,
            @Param("month") String month);

    int upsertEbookYearConfig(
            @Param("year") String year,
            @Param("month") String month,
            @Param("unitNo") String unitNo,
            @Param("subUnitNo") String subUnitNo);

}
