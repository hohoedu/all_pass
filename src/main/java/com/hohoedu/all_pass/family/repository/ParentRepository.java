package com.hohoedu.all_pass.family.repository;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.student._dto.StudentReqDTO.ParentJoinDTO;

@Mapper
public interface ParentRepository {
    public void insert(ParentJoinDTO parentDTO);
}
