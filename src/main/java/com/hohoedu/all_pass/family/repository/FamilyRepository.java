package com.hohoedu.all_pass.family.repository;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.ParentJoinDTO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FamilyRepository {

    void insert(ParentJoinDTO parentDTO);

    int countSiblingGroup(String groupKey);

    Integer findSiblingKey(String groupKey);

    int getMaxSiblingKey();

    void insertSiblingGroup(@Param("groupKey") String groupKey,
                            @Param("siblingKey") int siblingKey,
                            @Param("centerCode") String centerCode);

    void insertSibling(@Param("siblingKey") int siblingKey,
                       @Param("studentId") String studentId,
                       @Param("centerCode") String centerCode);
}
