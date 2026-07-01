package com.hohoedu.all_pass.secondary.repository;

import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SecondaryUserRepository {

    List<SecondaryDTO.TeacherDTO> findActiveTeachers();

    List<SecondaryDTO.TimetableRawDTO> findTimetable(
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month);
}
