package com.hohoedu.all_pass.student.repository;

import java.util.List;

import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;

import com.hohoedu.all_pass.student.model.StudentTransferSchedule;
import com.hohoedu.all_pass.student.model.TeacherAssign;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.StudentTransferHistory;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StudentJoinDTO;
import org.springframework.security.core.parameters.P;

@Mapper
public interface StudentRepository {

    public List<Student> findStudentByCenterCode(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode);

    public StudentWebRespDTO.StudentInfoDTO findStudentInfoByStudentId(@Param("studentId") String studentId);

    public StudentWebRespDTO.StudentPaymentDTO findStudentPaymentByStudentId(@Param("studentId") String studentId);


    public List<MainStudentDTO> selectStudentByUserCode(
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode);

    public List<MainStudentDTO> selectStudentByKey(
            @Param("timeTableKey") String timeTableKey,
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode);

    Integer findMaxAppIdSuffix(@Param("prefix") String prefix);

    public void insert(StudentJoinDTO student);

    public int statusHistoryInsert(StatusHistoryDTO historyDTO);

    public int studentStatusUpdate(StatusHistoryDTO historyDTO);

    int existsStudentClass(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    int insertStudentClass(TeacherAssign studentClass);

    int updateStudentClass(TeacherAssign studentClass);

    int updateStudentInfo(StudentWebReqDTO.StudentUpdateDTO req);

    int updateParent(
            @Param("first") String first,
            @Param("middle") String middle,
            @Param("last") String last,
            @Param("relationKey") String relationKey,
            @Param("studentId") String studentId
    );

    int updateStudentPayment(StudentWebReqDTO.StudentPaymentUpdateDTO req);

    int updateHanToActive(
            @Param("studentId") String studentId,
            @Param("entryHanDate") String entryHanDate
    );

    int updateHanToInactive(
            @Param("studentId") String studentId,
            @Param("inactiveHanDate") String inactiveHanDate,
            @Param("inactiveHanReason") String inactiveHanReason
    );

    int updateBookToActive(
            @Param("studentId") String studentId,
            @Param("entryBookDate") String entryBookDate
    );

    int updateBookToInactive(
            @Param("studentId") String studentId,
            @Param("inactiveBookDate") String inactiveBookDate,
            @Param("inactiveBookReason") String inactiveBookReason
    );

    TeacherAssign findTeacherAssign(@Param("studentId") String studentId);

    StudentWebRespDTO.TeacherDTO findTeacherAssignByStudentId(@Param("studentId") String studentId);

    int insertTeacherAssign(TeacherAssign teacherAssign);

    int updateTeacherAssign(ClassReqDTO.TeacherAssignUpdateDTO teacherAssign);

    StudentWebRespDTO.StudentStatusDTO findStatusByStudentId(@Param("studentId") String studentId);

    List<StudentInOutDTO> selectTransferStudents(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode);

    void findTransferStudentListByUserCode();

    List<StudentTransferDTO> findInOutByStudentId(@Param("studentId") Integer studentId);

    public void updateTransfer(
            @Param("studentId") String studentId,
            @Param("userCode") String userCode,
            @Param("classType") String classType,
            @Param("yy") String yy,
            @Param("mm") String mm);

    void insertTransferSchedule(
            @Param("studentId") String studentId,
            @Param("classType") String classType,
            @Param("fromUser") String fromUser,
            @Param("toUser") String toUser,
            @Param("moveAt") String moveAt,
            @Param("reason") String reason,
            @Param("createdBy") String createdBy
    );

    List<StudentTransferSchedule> findTodaySchedules(String today);

    void markAsApplied(Integer id);

    public void insertTransferHistory(StudentTransferHistory dto);

    public boolean existsByYm(String ym);

    int updateAttendance(StudentWebReqDTO.StudentAttendanceUpdateDTO dto);

    String findAttendanceKey(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("week") String week);

    // 출석 여부 체크
    public Integer countByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    List<ClassRespDTO.TimeRangeDTO> getStartClassTime(
            @Param("studentId") String studentId,
            @Param("year") String year,
            @Param("month") String month);

    Integer countByStudentAndDateAndTimeTable(
            @Param("studentId") String studentId,
            @Param("ymd") String ymd,
            @Param("timeTableKey") String timeTableKey);

    // 출석 insert
    public int checkinStudentAttendance(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate,
            @Param("inTime") String inTime,
            @Param("endTime") String endTime,
            @Param("attendanceKey") String attendanceKey,
            @Param("week") String week,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("timeTableKey") String timeTableKey);

    // 하원 여부 체크
    //TODO: 수정 필요
    public List<StudentAttendance> findByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    // 하원 update
    public int checkoutStudentAttendance(
            @Param("studentId") String studentId,
            @Param("outTime") String outTime,
            @Param("attendanceDate") String attendanceDate);

    public void insertSnapshotIfNotExists(String ym);

    public List<StudentSnapshotRespDTO> findAllStudentOverview(
            @Param("startYm") String startYm,
            @Param("endYm") String endYm);

    public List<StudentSnapshotRespDTO> findStudentOverview(
            @Param("startYm") String startYm,
            @Param("endYm") String endYm,
            @Param("userNo") int userNo);

    public Student findByStudentId(
            @Param("studentId") String studentId);

    public int updateAppTokenByStudentId(
            @Param("studentId") String studentId,
            @Param("appToken") String appToken);

    public StudentAppRespDTO.AppTokenRespDTO findAppTokenByAppId(
            @Param("appId") String appId);

    public StudentAppRespDTO.AppLoginViewDTO appLogin(
            @Param("appId") String appId);

    List<StudentAppRespDTO.AttendanceListRespDTO> findAttendanceList(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    List<StudentAppRespDTO.AttendanceMainRespDTO> findAttendanceMain(
            @Param("studentId") String studentId
    );

    String findSiblingKeyByStudentId(String studentId);

    List<StudentAppRespDTO.AppSiblingRespDTO> findSiblingBySiblingKey(String siblingKey);

    String getBookCodeByClassType(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm);
}
