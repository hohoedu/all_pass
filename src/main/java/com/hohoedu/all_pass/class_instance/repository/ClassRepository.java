package com.hohoedu.all_pass.class_instance.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;

@Mapper
public interface ClassRepository {

        public void registerClass(ClassReqDTO.ClassRegisterDTO classRegister);

        public TimeTableDTO existsByYearAndMonthAndPeriodNo(
                        @Param("periodNo") String periodNo,
                        @Param("year") String year,
                        @Param("month") String month,
                        @Param("dayname") String dayname);

        public void updateClass(
                        @Param("dto") ClassReqDTO.ClassRegisterDTO dto,
                        @Param("timeTableNo") Integer timeTableNo);

        public void addStudent(ClassReqDTO.AddStudentDTO addStudentDTO);

        public List<TimeTableDTO> findTimeTableBasic(
                        @Param("userNo") Integer userNo,
                        @Param("year") String year,
                        @Param("month") String month);

        public List<TimeTableDTO.StudentDTO> findStudentsByTimeTableNo(Integer timeTableNo);

        public int countByTimeTableNo(@Param("timeTableNo") Integer timeTableNo);

        public void deleteByAssignNo(@Param("timeTableAssignNo") Integer timeTableAssignNo);
}
