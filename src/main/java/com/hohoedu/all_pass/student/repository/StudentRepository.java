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
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StudentJoinDTO;

@Mapper
public interface StudentRepository {

    int countPendingStudents(String centerCode);

    StudentWebRespDTO.MainStudentStatusDTO findStudentStatus(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month,
            @Param("roleKey") String roleKey);

    public List<StudentWebRespDTO.StudentsListDTO> findStudentByCenterCode(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode);

    public StudentWebRespDTO.StudentInfoDTO findStudentInfoByStudentId(@Param("studentId") String studentId);

    public StudentWebRespDTO.StudentPaymentDTO findStudentPaymentByStudentId(@Param("studentId") String studentId);

    List<StudentWebRespDTO.StudentAttendanceDTO> findStudentAttendanceByStudentId(
            @Param("studentId") String studentId,
            @Param("year") String year,
            @Param("month") String month);

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

    List<StudentWebRespDTO.HanClass> findHanClasses(@Param("centerCode") String centerCode, @Param("studentId") String studentId);

    List<StudentWebRespDTO.BookClass> findBookClasses(@Param("centerCode") String centerCode, @Param("studentId") String studentId);

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

    StudentWebRespDTO.StudentSnapshotDTO findSnapshotByStudentId(
            @Param("studentId") String studentId);

    void insertWithdrawLog(
            @Param("studentId") String studentId,
            @Param("logType") String logType,
            @Param("snapshot") StudentWebRespDTO.StudentSnapshotDTO snapshot,
            @Param("reason") String reason,
            @Param("withdrawDate") String withdrawDate,
            @Param("userCode") String userCode);

    StudentWebRespDTO.WithdrawLogDTO findLatestWithdrawLog(
            @Param("studentId") String studentId,
            @Param("logType") String logType);

    // 학생 상태 직접 변경
    void updateStudentStatusDirect(
            @Param("studentId") String studentId,
            @Param("statusKey") String statusKey);

    // 한자 수강 복구
    void restoreHanState(
            @Param("studentId") String studentId,
            @Param("hanState") Integer hanState,
            @Param("hanClass") String hanClass,
            @Param("hanTeacher") String hanTeacher,
            @Param("entryHanDate") String entryHanDate);

    // 독서 수강 복구
    void restoreBookState(
            @Param("studentId") String studentId,
            @Param("bookState") Integer bookState,
            @Param("bookClass") String bookClass,
            @Param("bookTeacher") String bookTeacher,
            @Param("entryBookDate") String entryBookDate);

    // 복구 처리자 기록
    void updateWithdrawLogRestored(
            @Param("id") Long id,
            @Param("userCode") String userCode);

    void updatePendingIsDeletedByStudentId(String studentId);

    void updateStudentStatusToActive(String studentId);

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
            @Param("createdBy") String createdBy,
            @Param("centerCode") String centerCode
    );

    List<StudentTransferSchedule> findTodaySchedules(String today);

    void markAsApplied(Integer id);

    public void insertTransferHistory(StudentTransferHistory dto);

    List<StudentWebRespDTO.TransferHistoryDTO> selectTransferHistory(
            @Param("centerCode") String centerCode,
            @Param("yearMonth") String yearMonth);

    public boolean existsByYm(String ym);

    int updateAttendance(StudentWebReqDTO.StudentAttendanceUpdateDTO dto);

    String findAttendanceKey(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("week") String week);

    int countStudent(String studentId);

    void cancelJoinStudent(String studentId);

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

    public List<StudentAttendance> findByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    public int checkinStudentAttendance(
            @Param("studentId") String studentId,
            @Param("inTime") String inTime,
            @Param("endTime") String endTime,
            @Param("attendanceKey") String attendanceKey,
            @Param("week") String week,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("timeTableKey") String timeTableKey);

    List<StudentAppRespDTO.RemedialDTO> findRemedialByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("ymd") String ymd
    );

    int checkinRemedialAttendance(
            @Param("remedialKey") String remedialKey,
            @Param("inTime") String inTime,
            @Param("attendanceKey") String attendanceKey
    );

    int checkoutRemedialAttendance(
            @Param("remedialKey") String remedialKey,
            @Param("outTime") String outTime
    );

    int updateIsRemedial(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("week") String week,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    StudentAppRespDTO.AttendanceInfoDTO findWeekInfoByDate(
            @Param("absenceDate") String absenceDate,
            @Param("centerCode") String centerCode
    );

    // 하원 여부 체크


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

    List<StudentWebRespDTO.SiblingSearchRespDTO> searchFamily(
            @Param("searchKey") String searchKey,
            @Param("searchValue") String searchValue,
            @Param("centerCode") String centerCode
    );

    boolean existsFamilyLink(
            @Param("studentId") String studentId,
            @Param("siblingId") String siblingId
    );

    String getSiblingGroupKey(@Param("studentId") String studentId);

    void insertSiblingMember(
            @Param("groupKey") String groupKey,
            @Param("studentId") String studentId,
            @Param("centerCode") String centerCode
    );

    String generateNewGroupKey();

    void mergeGroups(
            @Param("targetGroupKey") String targetGroupKey,
            @Param("sourceGroupKey") String sourceGroupKey
    );

    String getCenterCode(@Param("studentId") String studentId);


    List<StudentWebRespDTO.SiblingSearchRespDTO> getSiblingList(@Param("studentId") String studentId);

    int countGroupMembers(@Param("groupKey") String groupKey);

    void deleteGroup(@Param("groupKey") String groupKey);

    void deleteSiblingMember(
            @Param("groupKey") String groupKey,
            @Param("studentId") String studentId
    );

    int countByQrNumber(
            @Param("qrNumber") String qrNumber,
            @Param("centerCode") String centerCode);

    int countInUse(
            @Param("qrNumber") String qrNumber,
            @Param("centerCode") String centerCode);

    int registerQrNumber(
            @Param("studentId") String studentId,
            @Param("qrNumber") String qrNumber,
            @Param("centerCode") String centerCode);

    int updateQrNumber(
            @Param("studentId") String studentId,
            @Param("qrNumber") String qrNumber);

    void unlinkQrByStudentId(
            @Param("studentId") String studentId,
            @Param("centerCode") String centerCode);
}


