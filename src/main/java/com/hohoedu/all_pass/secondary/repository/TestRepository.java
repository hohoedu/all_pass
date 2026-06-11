package com.hohoedu.all_pass.secondary.repository;

import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestRepository {

    List<SecondaryDTO.TestDTO> findAll();
}
