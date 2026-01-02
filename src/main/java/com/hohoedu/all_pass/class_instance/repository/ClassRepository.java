package com.hohoedu.all_pass.class_instance.repository;

import java.util.List;
import java.util.Map;

import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.*;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.class_instance.TimeTable;
import com.hohoedu.all_pass.user.User;
import org.springframework.security.core.parameters.P;

@Mapper
public interface ClassRepository {

    List<ClassRespDTO.MainClassSummaryDTO> findClassSummary(
            @Param("year") String year,
            @Param("month") String month,
            @Param("dayname") String dayname,
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode
    );

    List<ClassRespDTO.ClassWeekDTO> getClassWeek(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode
    );

    void insertClassWeek(ClassReqDTO.SetWeekDTO dto);

    void updateClassWeek(ClassReqDTO.SetWeekDTO dto);

    public void registerClass(ClassReqDTO.ClassRegisterDTO classRegister);

    List<ClassRespDTO.ClassUnitDTO> findClassUnitMap();

    List<ClassRespDTO.ClassUnitDTO> findPersonUnit(
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("classKey") String classKey
    );

    List<ClassRespDTO.ClassUnitDTO> findPersonUnitsInRange(
            @Param("centerCode") String centerCode,
            @Param("startYear") String startYear,
            @Param("startMonth") String startMonth,
            @Param("endYear") String endYear,
            @Param("endMonth") String endMonth,
            @Param("classKey") String classKey
    );

    public void createTimeTableKey(TimeTableCode entity);

    public ClassRespDTO.TimeTableDTO existsByYearAndMonthAndPeriodNo(
            @Param("periodNo") String periodNo,
            @Param("year") String year,
            @Param("month") String month,
            @Param("dayname") String dayname,
            @Param("userCode") String userCode);

    public int updateClass(
            @Param("dto") ClassReqDTO.ClassRegisterDTO dto,
            @Param("timeTableKey") String timeTableKey,
            @Param("userCode") String userCode);

    public int updateLabel(
            @Param("label") String label,
            @Param("timeTableKey") String timeTableKey);

    public void addStudent(ClassReqDTO.AddStudentDTO addStudentDTO);

    ClassRespDTO.ClassInfoDTO findClassInfoByTimeTableKeyAndStudentId(
            @Param("timeTableKey") String timeTableKey,
            @Param("studentId") String studentId,
            @Param("centerCode") String centerCode);

    public void insertMonthlyScore(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("timeTableKey") String timeTableKey);

    public List<ClassRespDTO.TimeTableDTO> findTimeTableBasic(
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month);

    Map<String, Object> findUnitByClassAndUnit(
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey
    );

    Map<String, Object> findNextUnitById(@Param("id") int id);

    Map<String, String> findReadingMapping(
            @Param("classKey") String classKey,
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    public List<ClassRespDTO.TimeTableDTO.StudentDTO> findStudentsByTimeTableKey(String timeTableKey);

    public int countByTimeTableKey(@Param("timeTableKey") String timeTableKey);

    StudentWebRespDTO.TransferTimeTableInfoDTO findTimeTableKeyByStudentId(
            @Param("studentId") String studentId,
            @Param("classType") String classType,
            @Param("yy") String yy,
            @Param("mm") String mm);

    public int deleteByKeyAndStudentId(
            @Param("timeTableKey") String timeTableKey,
            @Param("studentId") String studentId);

    int deleteTimeTableRow(
            @Param("timeTableKey") String timeTableKey);

    public List<TimeTable> findTimeTable(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("userNo") String userNo);

    public List<TimeTableCode> findTimeTableCodeByUserCode(
            @Param("userCode") String userCode);

    List<ClassRespDTO.ComClassStudentDTO> findComClassStudentsByTimeTableKey(
            @Param("timeTableKey") String timeTableKey,
            @Param("userCode") String userCode
    );

    List<ClassRespDTO.ComClassStudentDTO> findComClassStudentsByUserCode(
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    int updateTimeTableAssign(
            @Param("timeTableKey") String timeTableKey,
            @Param("studentId") String studentId,
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey);

    // 날짜별 선생님별 수업 조회
    public List<ClassRespDTO.RecordLabelDTO> findTimeTableByUserCode(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("dayName") String dayName,
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode);

    public List<ClassRespDTO.RecordStudentDTO> findRecordStudentByKey(
            @Param("timeTableKey") String timeTableKey,
            @Param("week") String week);

    public List<ClassRespDTO.RemedialDTO> findRemedialByUserNo(
            @Param("year") String year,
            @Param("month") String month);

    public int updateRemedialAction(
            @Param("remedialKey") String remedialKey,
            @Param("action") boolean action);

    public void updateRemedialDate(
            @Param("remedialKey") String remedialKey,
            @Param("remedialDate") String remedialDate);


    // 선생님별 클래스 코드 조회
    public List<ClassRespDTO.TimeTableLabelDTO> findClassLabelByUserCode(
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("count") String count);

    public List<ClassRespDTO.TimeTableLabelDTO> findClassLabelByUserCodeAndDayname(
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("dayname") String dayname);

    List<ClassRespDTO.TimeTableLabelDTO> findInfantClassLabel(
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    public ClassRespDTO.BeforeClassRespDTO findBeforeClass(
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("week") String week,
            @Param("timeTableKey") String timeTableKey);

    public void createAttendance(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    List<ClassWeek> findClassWeekByCenter(
            @Param("centerCode") String centerCode,
            @Param("year") String year,
            @Param("month") String month);

    void updateAttendance(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("attendanceDate") String attendanceDate,
            @Param("week") String week);

    void updateAfterSend(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("week") String week);

    public ClassRespDTO.AfterClassRespDTO findAfterClass(
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("week") String week,
            @Param("timeTableKey") String timeTableKey);

    public List<ClassRespDTO.MonthlyStudentDTO> findStudentByClassCode(
            @Param("timeTableKey") String timeTableKey);

    int updateMonthlyScore(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("score") ClassReqDTO.ClassMonthlyScoreDTO.MonthlyScoreDTO score);

    Map<String, String> getMonthlyFeedback(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    MonthlyResult findMonthlyResult(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    void insertMonthlyResult(MonthlyResult monthlyResult);

    void updateMonthlyResult(MonthlyResult monthlyResult);

    List<Map<String, Object>> findMonthlyPreview(ClassReqDTO.MonthlyPreviewDTO dto);

    public List<ClassAppRespDTO.ClassInfoRespDTO> findClassInfoByStudentId(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm);

    List<ClassAppRespDTO.BeforeClassRespDTO> findBeforeClassByStudentId(
            @Param("studentId") String studentId,
            @Param("count") int count);

    List<ClassAppRespDTO.AfterClassRespDTO> findAfterClassByStudentId(
            @Param("studentId") String studentIdm,
            @Param("count") int count);

    public ClassRespDTO.RawClassDTO findClassByTimeTableKey(@Param("timeTableKey") String timeTableKey);

    ClassRespDTO.InfantHanDTO findInfantHan(
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("year") String year);

    ClassRespDTO.InfantBookDTO findInfantBook(
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("year") String year);

    List<ClassRespDTO.InfantHanDTO.StudentInfo> findInfantHanStudents(String TimeTableKey);

    List<ClassRespDTO.InfantBookDTO.StudentInfo> findInfantBookStudents(String TimeTableKey);

    int existsInfantSendHistory(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey
    );

    int createInfantSendHistory(InfantSendHistory history);

    int countInfantHan(@Param("studentId") String studentId,
                       @Param("timeTableKey") String timeTableKey);

    int countInfantBook(@Param("studentId") String studentId,
                        @Param("timeTableKey") String timeTableKey);

    int findInfantSendId(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey);

    void insertInfantHanNotice(@Param("dto") ClassReqDTO.InfantSaveReqDTO dto,
                               @Param("centerCode") String centerCode,
                               @Param("userCode") String userCode,
                               @Param("studentId") String studentId,
                               @Param("sendId") Integer sendId);

    void insertInfantBookNotice(@Param("dto") ClassReqDTO.InfantSaveReqDTO dto,
                                @Param("centerCode") String centerCode,
                                @Param("userCode") String userCode,
                                @Param("studentId") String studentId,
                                @Param("sendId") Integer sendId);

    public void insertBeforeClassNotice(ClassReqDTO.BeforeClassNoticeDTO dto);

    public void insertAfterClassNotice(ClassReqDTO.AfterClassNoticeDTO dto);

    List<UnitCode> findUnitCodeForPerson();

    List<ClassCode> findClassListExcludeMid();

    List<UnitCode> findHanLevelCode();

    ClassRespDTO.BasicTimeTableInfo findBasicTimeTableInfo(
            @Param("timeTableKey") String timeTableKey,
            @Param("centerCode") String centerCode);

    List<ClassAppRespDTO.ClinicListRespDTO> findClinicList(
            @Param("studentId") String studentId,
            @Param("ym") String ym
    );

    List<ClassAppRespDTO.ClinicResultRespDTO> findClinicResult(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    List<ClassAppRespDTO.ClinicTotalRespDTO> findClinicTotal(
            @Param("studentId") String studnetId,
            @Param("sYear") String sYear,
            @Param("sMonth") String sMonth,
            @Param("eYear") String eYear,
            @Param("eMonth") String eMonth
    );
}
