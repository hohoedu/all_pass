package com.hohoedu.all_pass.app;

import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppRepository {

    List<ClassAppRespDTO.BookListMainRawDTO> findBookMainInfo(
            @Param("gradeKey") String gradeKey);


    List<ClassAppRespDTO.BookListRawDTO> findBookInfo(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm);
}
