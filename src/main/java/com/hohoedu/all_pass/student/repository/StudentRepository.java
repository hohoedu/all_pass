package com.hohoedu.all_pass.student.repository;

import java.util.List;

import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;

import com.hohoedu.all_pass.student.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StudentJoinDTO;
import org.springframework.security.core.parameters.P;

@Mapper
public interface StudentRepository {

    StudentWebRespDTO.MainStudentStatusDTO findStudentStatus(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month);

    public List<StudentWebRespDTO.StudentsListDTO> findStudentByCenterCode(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode);

    public StudentWebRespDTO.StudentInfoDTO findStudentInfoByStudentId(@Param("studentId") String studentId);

    public StudentWebRespDTO.StudentPaymentDTO findStudentPaymentByStudentId(@Param("studentId") String studentId);

    public List<StudentWebRespDTO.StudentConsultDTO> findStudentConsultByStudentId(@Param("studentId") String studentId);

    void createPendingStudent(PendingStudent pendingStudent);

    void insertPendingStudent(PendingStudent pendingStudent);

    StudentWebRespDTO.PendingStudentRespDTO findStudentByInviteCode(String inviteCode);

    int checkDuplicateStudent(
            @Param("studentName") String studentName,
            @Param("parentTelMiddle") String parentTelMiddle,
            @Param("parentTelLast") String parentTelLast);

    public List<MainStudentDTO> selectStudentByUserCode(
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode);

    public List<MainStudentDTO> selectStudentByKey(
            @Param("timeTableKey") String timeTableKey,
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode);

    Integer findMaxAppIdSuffix(@Param("prefix") String prefix);

    public void insert(StudentJoinDTO student);

    void createTeacherAssign(
            @Param("studentId") String studentId,
            @Param("centerCode") String centerCode);

    InviteTracking findByInviteCode(String inviteCode);

    PendingStudent findPendingStudentBySendKey(String sendKey);

    void updatePendingStudentOnRegister(String sendKey, String studentId, String name, String gradeKey);

    public int statusHistoryInsert(StatusHistoryDTO historyDTO);

    public int studentStatusUpdate(StatusHistoryDTO historyDTO);

    int existsStudentClass(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    int insertStudentClass(TeacherAssign studentClass);

    int updateStudentClass(TeacherAssign studentClass);

    boolean existsByAppIdAndNotStudentId(
            @Param("appId") String appId,
            @Param("studentId") String studentId);

    int updateStudentInfo(StudentWebReqDTO.StudentUpdateDTO req);

    int updateParent(
            @Param("first") String first,
            @Param("middle") String middle,
            @Param("last") String last,
            @Param("relationKey") String relationKey,
            @Param("studentId") String studentId
    );

    int updateStudentPayment(StudentWebReqDTO.StudentPaymentUpdateDTO req);

    List<StudentWebRespDTO.HanClass> findHanClasses(@Param("centerCode") String centerCode);

    List<StudentWebRespDTO.BookClass> findBookClasses(@Param("centerCode") String centerCode);

    List<StudentWebRespDTO.HanTeacher> findHanTeachers(@Param("centerCode") String centerCode);

    List<StudentWebRespDTO.BookTeacher> findBookTeachers(@Param("centerCode") String centerCode);

    int updateHanToActive(
            @Param("studentId") String studentId,
            @Param("entryHanDate") String entryHanDate,
            @Param("userCode") String userCode
    );

    int updateHanToInactive(
            @Param("studentId") String studentId,
            @Param("inactiveHanDate") String inactiveHanDate,
            @Param("inactiveHanReason") String inactiveHanReason,
            @Param("userCode") String userCode
    );

    int updateBookToActive(
            @Param("studentId") String studentId,
            @Param("entryBookDate") String entryBookDate,
            @Param("userCode") String userCode
    );

    int updateBookToInactive(
            @Param("studentId") String studentId,
            @Param("inactiveBookDate") String inactiveBookDate,
            @Param("inactiveBookReason") String inactiveBookReason,
            @Param("userCode") String userCode
    );

    void updateHanClassAndTeacher(
            @Param("studentId") String studentId,
            @Param("hanClassKey") String hanClassKey,
            @Param("hanTeacherCode") String hanTeacherCode
    );

    void updateBookClassAndTeacher(
            @Param("studentId") String studentId,
            @Param("bookClassKey") String bookClassKey,
            @Param("bookTeacherCode") String bookTeacherCode
    );

    void updatePendingIsDeletedByStudentId(String studentId);

    TeacherAssign findTeacherAssign(@Param("studentId") String studentId);

    StudentWebRespDTO.TeacherDTO findTeacherAssignByStudentId(@Param("studentId") String studentId);

    boolean existsTransferSchedule(String studentId, String fromUserCode);

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

    List<StudentWebRespDTO.PendingStudentRespDTO> findPendingStudent(String centerCode);

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

    List<StudentWebRespDTO.SiblingInfoDTO> findSiblingByStudentId(
            @Param("studentId") String studentId);

    public int updateAppTokenByStudentId(
            @Param("studentId") String studentId,
            @Param("appToken") String appToken);

    public StudentAppRespDTO.AppTokenRespDTO findAppTokenByAppId(
            @Param("appId") String appId);

    StudentAppRespDTO.AppTokenRespDTO findAppTokenByStudentId(
            @Param("studentId") String studentId);


    String findAppIdByLog(@Param("appId") String appId);

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

    String findByGradeKey(String gradeKey);

    StudentWebRespDTO.WithdrawCountDTO selectWithdrawCounts(StudentWebReqDTO.WithdrawReqDTO req);

    List<StudentWebRespDTO.WithdrawItemDTO> selectJoinList(StudentWebReqDTO.WithdrawReqDTO req);

    List<StudentWebRespDTO.WithdrawItemDTO> selectWithdrawList(StudentWebReqDTO.WithdrawReqDTO req);

    List<StudentWebRespDTO.WithdrawItemDTO> selectTransferInList(StudentWebReqDTO.WithdrawReqDTO req);

    List<StudentWebRespDTO.WithdrawItemDTO> selectTransferOutList(StudentWebReqDTO.WithdrawReqDTO req);

    List<StudentWebRespDTO.WithdrawItemDTO> selectGraduateList(StudentWebReqDTO.WithdrawReqDTO req);

    List<StudentWebRespDTO.SearchRespDTO> searchByStudentName(String studentName);

    void updateAppIdLog(StudentWebReqDTO.UpdateAppIdLogDTO req);

    int countByPendingId(Integer id);

    void insertToPendingDel(
            @Param("id") Integer id,
            @Param("userCode") String userCode);

    void deleteByPendingId(Integer id);

    void updateStudentBillingPhone(
            @Param("studentId") String studentId,
            @Param("phone") String phone);


}
