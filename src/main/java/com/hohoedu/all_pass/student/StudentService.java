package com.hohoedu.all_pass.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.handler.exception.AppRestfulException;
import com.hohoedu.all_pass._core.handler.exception.CustomRestfulException;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.repository.CenterRepository;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.AttendanceCode;
import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import com.hohoedu.all_pass.family.FamilyService;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student._dto.app.StudentAppReqDTO;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student.model.*;
import com.hohoedu.all_pass.student.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.repository.ClassCodeJpaRepository;
import com.hohoedu.all_pass.family.model.RelationCode;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.repository.UserRepository;
import com.hohoedu.all_pass.family.repository.RelationJpaRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final RelationJpaRepository relationJpaRepository;
    private final UserRepository userRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;
    private final SnapshotRepository snapshotRepository;
    private final SnapshotJpaRepository snapshotJpaRepository;
    private final CenterRepository centerRepository;
    private final DateConfig dateConfig;
    private final FamilyService familyService;
    private final PaymentRepository paymentRepository;

    public List<Student> findStudentByCenterCode(String year, String month, String centerCode, String userCode) {

        List<Student> student = studentRepository.findStudentByCenterCode(year, month, centerCode, userCode);

        return student;
    }

    public List<MainStudentDTO> getStudentsByKey(String timeTableKey, String userCode, String centerCode) {
        List<MainStudentDTO> rows = studentRepository.selectStudentByKey(timeTableKey, userCode, centerCode);
        return rows;
    }

    public List<MainStudentDTO> getStudentsByUserCode(String userCode, String centerCode) {
        System.out.println(userCode);
        List<MainStudentDTO> rows = studentRepository.selectStudentByUserCode(userCode, centerCode);
        return rows;
    }

    public StudentWebRespDTO.StudentDTO findStudentByStudentId(String studentId) {

        StudentWebRespDTO.StudentDTO student = studentRepository.findStudentByStudentId(studentId);

        return student;
    }

    public void studentInsert(StudentWebReqDTO.StudentJoinDTO studentDTO, StudentWebReqDTO.ParentJoinDTO parentDTO) {
        String today = dateConfig.currentYearMonth().get("today");
        String random = UUID.randomUUID().toString().replace("-", "");
        String last5 = random.substring(random.length() - 5).toUpperCase();
        String code = studentDTO.getCenterCode() + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + last5;
        studentDTO.setStudentId(code);

        studentDTO.setAppId(parentDTO.getParentTelMiddle() + parentDTO.getParentTelLast() + 0);
        studentDTO.setAppPassword(
                DigestUtils.sha256Hex(parentDTO.getParentTelLast())
        );
        studentDTO.setEntryHanDate(today);

        studentRepository.insert(studentDTO);
        parentDTO.setStudentId(studentDTO.getStudentId());
        familyService.parentInsert(parentDTO);
    }

    public List<GradeCode> findGrade() {

        List<GradeCode> gradeCodes = gradeJpaRepository.findAll();
        return gradeCodes;
    }

    public List<RelationCode> findRelation() {

        List<RelationCode> relationCodes = relationJpaRepository.findAll();
        return relationCodes;
    }

    public List<ClassCode> findClassCode() {
        List<ClassCode> classCodes = classCodeJpaRepository.findAll();
        return classCodes;
    }

    public List<StudentInOutDTO> findAllInOut(String centerCode) {
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");
        List<StudentInOutDTO> students = studentRepository.selectTransferStudents(centerCode, yy, mm);
        return students;
    }

    public List<User> findTeacher(String centerCode) {
        List<User> users = userRepository.findUserByCenterCode(centerCode);
        System.out.println(users);
        return users;
    }

    public StudentWebRespDTO.StudentStatusDTO statusInsert(StudentWebReqDTO.StatusHistoryDTO historyDTO, String userCode) {
        int updateResult = studentRepository.studentStatusUpdate(historyDTO);
        System.out.println(updateResult);
        if (updateResult == 0) {
            System.out.println("업데이트 실패");
        } else {
            System.out.println("업데이트 성공");
        }

        historyDTO.setUserCode(userCode);

        int insertResult = studentRepository.statusHistoryInsert(historyDTO);
        if (insertResult == 0) {
            System.out.println("인서트 실패");
        } else {
            System.out.println("인서트 성공");
        }

        StudentWebRespDTO.StudentStatusDTO respDTO = studentRepository.findStatusByStudentId(historyDTO.getStudentId());

        return respDTO;
    }

    public List<GradeCode> getGrade() {
        List<GradeCode> gradeCodes = gradeJpaRepository.findAll();
        return gradeCodes;
    }

    public String insertStudentClass(ClassRespDTO.ClassInfoDTO dto, String studentId, String yy, String mm) {
        Integer fee = paymentRepository.findFeeByClassKey(dto.getClassKey(), dto.getCenterCode());
        Integer materialFee = 20000;

        StudentClass.StudentClassBuilder builder = StudentClass.builder()
                .student(Student.builder().studentId(studentId).build())
                .yy(yy)
                .mm(mm);

            if ("1".equals(dto.getClassType())) {
                builder
                        .hanClassCode(ClassCode.builder().classKey(dto.getClassKey()).build())
                        .hanUser(User.builder().userCode(dto.getUserCode()).build())
                        .hanFee(fee)
                        .hanMaterialFee(materialFee);

            } else if ("2".equals(dto.getClassType())) {
                builder
                        .bookClassCode(ClassCode.builder().classKey(dto.getClassKey()).build())
                        .bookUser(User.builder().userCode(dto.getUserCode()).build())
                        .bookFee(fee)
                        .bookMaterialFee(materialFee);
            }

        StudentClass studentClass = builder.build();

        StudentClass existing = studentRepository.findStudentClassByStudentId(studentId, yy, mm);

        if (existing != null)
            studentRepository.updateStudentClass(studentClass);
        else
            studentRepository.insertStudentClass(studentClass);

        return "ok";
    }

    public List<StudentTransferDTO> findInOutByStudentId(Integer studentId) {
        List<StudentTransferDTO> responseDTO = studentRepository.findInOutByStudentId(studentId);
        return responseDTO;

    }

    public void transferStudent(StudentWebReqDTO.StudentTransferDTO reqDto) {
        try {
            String year = dateConfig.currentYearMonth().get("currentYear");
            String month = dateConfig.currentYearMonth().get("currentMonth");
            System.out.println(year + "-" + month);
            if (reqDto.getSelectedHan() != null) {
                for (String studentId : reqDto.getStudents()) {

                    // 시간표 삭제
                    // 타임테이블 키 조회
                    StudentWebRespDTO.TransferTimeTableInfoDTO dto = classRepository.findTimeTableKeyByStudentId(studentId, reqDto.getSelectedHan(), year, month);
                    classRepository.deleteByKeyAndStudentId(dto.getTimeTableKey(), studentId);
                    // student_class 변경
                    studentRepository.updateTransfer(studentId, reqDto.getUserCode(), reqDto.getSelectedHan(), year, month);
                    // history 저장 (누가 변경했는지 없음
                    StudentTransferHistory history = StudentTransferHistory.builder()
                            .student(Student.builder().studentId(studentId).build())
                            .fromUser(User.builder().userCode(dto.getUserCode()).build())
                            .toUser(User.builder().userCode(reqDto.getUserCode()).build())
                            .classCode(ClassCode.builder().classKey(dto.getClassKey()).build())
                            .classType(reqDto.getSelectedHan())
                            .transferReason(reqDto.getTransferReason())
                            .moveAt(reqDto.getMoveAt())
                            .build();
                    studentRepository.insertTransferHistory(history);

                }
            }
            if (reqDto.getSelectedBook() != null) {
                for (String studentId : reqDto.getStudents()) {
                    // 시간표 삭제
                    // 타임테이블 키 조회
                    StudentWebRespDTO.TransferTimeTableInfoDTO dto = classRepository.findTimeTableKeyByStudentId(studentId, reqDto.getSelectedBook(), year, month);
                    classRepository.deleteByKeyAndStudentId(dto.getTimeTableKey(), studentId);
                    // student_class 변경
                    studentRepository.updateTransfer(studentId, reqDto.getUserCode(), reqDto.getSelectedBook(), year, month);
                    // history 저장 (누가 변경했는지 없음)
                    StudentTransferHistory history = StudentTransferHistory.builder()
                            .student(Student.builder().studentId(studentId).build())
                            .fromUser(User.builder().userCode(dto.getUserCode()).build())
                            .toUser(User.builder().userCode(reqDto.getUserCode()).build())
                            .classCode(ClassCode.builder().classKey(dto.getClassKey()).build())
                            .classType(reqDto.getSelectedBook())
                            .transferReason(reqDto.getTransferReason())
                            .moveAt(reqDto.getMoveAt())
                            .build();

                    studentRepository.insertTransferHistory(history);

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public StudentAppRespDTO.AppTokenRespDTO findAppTokenByAppId(String appId) {
        StudentAppRespDTO.AppTokenRespDTO respDTO = studentRepository.findAppTokenByAppId(appId);
        return respDTO;
    }

    public boolean checkinStudent(StudentAppReqDTO.StudentAttendanceDTO dto, Student student) {

        Integer count = studentRepository.countByStudentAndDate(student.getStudentId(), dto.getYmd());
        if (count != null && count > 0) {
            return false;
        }
        studentRepository.checkinStudentAttendance(student.getStudentId(), dto.getHhmm(), dto.getYmd());

        return true;
    }

    public int checkoutStudent(StudentAppReqDTO.StudentAttendanceDTO dto, Student student) {
        List<StudentAttendance> sa = studentRepository.findByStudentAndDate(student.getStudentId(), dto.getYmd());
        if (sa == null) {
            System.out.println("등원 기록 없음");
            return 8888;
        }
        if (sa.get(0).getOutTime() != null) {
            System.out.println("이미 처리 됨");
            return 6666;
        }
        System.out.println("이제 업데이트");
        studentRepository.checkoutStudentAttendance(student.getStudentId(), dto.getHhmm(), dto.getYmd());
        return 0000;
    }

    // ================================================================================================================
    private LocalDateTime endOfMonthDateTime(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4));
        int m = Integer.parseInt(ym.substring(4, 6));
        LocalDate end = YearMonth.of(y, m).atEndOfMonth();
        return end.atTime(23, 59, 59);
    }

    /**
     * (A) 특정 월 실시간 집계 후 스냅샷 저장/갱신
     */
    @Transactional
    public void upsertSnapshot(String centerCode, String ym) {
        LocalDateTime monthEnd = endOfMonthDateTime(ym);
        Map<String, Object> r = snapshotRepository.aggregateAtMonthEnd(centerCode, monthEnd);

        int total = toInt(r.get("total_count"));
        int active = toInt(r.get("active_count"));
        int rest = toInt(r.get("rest_count"));
        int withdrawn = toInt(r.get("withdrawn_count"));
        int wait = toInt(r.get("wait_count"));

        Center center = centerRepository.findByCenterCode(centerCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 센터: " + centerCode));

        StudentSnapshotId id = new StudentSnapshotId(ym, centerCode);
        StudentSnapshot entity = StudentSnapshot.builder()
                .id(id)
                .center(center)
                .totalCount(total)
                .activeCount(active)
                .restCount(rest)
                .withdrawnCount(withdrawn)
                .waitCount(wait)
                .build();

        snapshotJpaRepository.save(entity);
    }

    /**
     * (B) 구간 스냅샷 조회 (포함)
     */
    @Transactional(readOnly = true)
    public List<StudentSnapshotRespDTO> getSnapshotRange(String centerCode, String fromYm, String toYm) {
        return snapshotJpaRepository
                .findByIdCenterCodeAndIdSnapshotYmBetweenOrderByIdSnapshotYm(centerCode, fromYm, toYm)
                .stream()
                .map(s -> new StudentSnapshotRespDTO(
                        s.getSnapshotYm(),
                        s.getCenterCode(),
                        s.getTotalCount(),
                        s.getActiveCount(),
                        s.getRestCount(),
                        s.getWithdrawnCount(),
                        s.getWaitCount()
                ))
                .toList();
    }

    private int toInt(Object v) {
        return v == null ? 0 : ((Number) v).intValue();
    }


    public void saveSnapshot(String ym) {
        studentRepository.insertSnapshotIfNotExists(ym);
    }

    public List<StudentSnapshotRespDTO> getSnapshot(String startYm, String endYm, Integer userNo) {
        List<StudentSnapshotRespDTO> studentOverview = new ArrayList<>();
        if (userNo == null) {
            studentOverview = studentRepository.findAllStudentOverview(startYm, endYm);
        } else {
            studentOverview = studentRepository.findStudentOverview(startYm, endYm, userNo);
        }
        return studentOverview;
    }

    public Student findByStudentId(String studentId) {
        Student student = studentRepository.findByStudentId(studentId);
        return student;
    }

    // 학생 앱 토큰 등록
    public void updateAppToken(StudentAppReqDTO.AppTokenReqDTO dto) {
        Student student = studentRepository.findByStudentId(dto.getId());

        if (student == null) {
            return;
        }

        if (student.getAppToken() == null || !student.getAppToken().equals(dto.getToken())) {
            int updateResult = studentRepository.updateAppTokenByStudentId(dto.getId(), dto.getToken());
            if (updateResult > 0) {
                System.out.println("토큰 등록 완료");
            } else {
                System.out.println("이미 등록된 토큰");
            }
        }

    }


    // ================================== app ================================== //

    public StudentAppRespDTO.AppLoginRespDTO checkAppIdAndPassword(String appId, String password) {
        StudentAppRespDTO.AppLoginViewDTO row = studentRepository.appLogin(appId);
        System.out.println("=================================");
        System.out.println("== appId: " + appId + " password: " + password);
        System.out.println("=================================");

        if (row == null || !password.equals(row.getAppPassword())) {
            throw new AppRestfulException("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.FORBIDDEN);
        }

        System.out.println("=================================");
        System.out.println(row.getGradeKey());
        System.out.println("=================================");

        StudentAppRespDTO.AppLoginRespDTO respDTO = StudentAppRespDTO.AppLoginRespDTO.builder()
                .stuid(row.getStudentId())
                .name(row.getStudentName())
                .cid(row.getCenterCode())
                .cname(row.getCenterName())
                .hak(row.getGradeKey())
                .ihak(row.getGradeKey())
                .profileimg("1")
                .appid(row.getAppId())
                .build();

        return respDTO;
    }
}
