package com.hohoedu.all_pass.student.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface SnapshotRepository {

    Map<String, Object> aggregateAtMonthEnd(
            @Param("centerCode") String centerCode,
            @Param("monthEnd") LocalDateTime monthEnd
    );

}
