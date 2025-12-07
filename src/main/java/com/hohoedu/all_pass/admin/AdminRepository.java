package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminRepository {
    int insertPersonYear(
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("subUnitKey") String subUnitKey);

    List<AdminRespDTO.PersonYearDTO> selectPersonYear(
            @Param("centerCode") String centerCode,
            @Param("yy") String yy
    );

    List<SubjectCode> findSubject();

    int upsertBookSuggest(AdminReqDTO.BookSuggestDTO dto);
}
