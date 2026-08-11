package com.hohoedu.all_pass.third.repository;

import com.hohoedu.all_pass.third._dto.ThirdDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ThirdEbookRepository {

    List<ThirdDTO.KeycodeRawDTO> findKeycodeList(
            @Param("preschoolCode") String preschoolCode,
            @Param("orderym") String orderym);
}
