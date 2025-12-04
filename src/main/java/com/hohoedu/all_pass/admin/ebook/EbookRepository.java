package com.hohoedu.all_pass.admin.ebook;

import com.hohoedu.all_pass.admin.ebook._dto.EbookReqDTO;
import com.hohoedu.all_pass.admin.ebook._dto.EbookRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EbookRepository {
    int insertPersonYear(
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("subUnitKey") String subUnitKey);

    List<EbookRespDTO.PersonYearDTO> selectPersonYear(
            @Param("centerCode") String centerCode,
            @Param("yy") String yy
    );
}
