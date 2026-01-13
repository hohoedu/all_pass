package com.hohoedu.all_pass.student;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.handler.exception.AppRestfulException;
import com.hohoedu.all_pass._core.handler.exception.Exception400;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.repository.CenterRepository;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassWeek;
import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import com.hohoedu.all_pass.family.FamilyService;
import com.hohoedu.all_pass.payment.PaymentService;
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
    private final PaymentService paymentService;

    public List<Student> findStudentByCenterCode(String year, String month, String centerCode, String userCode) {

        List<Student> student = studentRepository.findStudentByCenterCode(year, month, centerCode, userCode);

        return student;
    }

    public List<MainStudentDTO> getStudentsByKey(String timeTableKey, String userCode, String centerCode) {
        List<MainStudentDTO> rows = studentRepository.selectStudentByKey(timeTableKey, userCode, centerCode);
        return rows;
    }

    // 학생 리스트 조회
    public List<MainStudentDTO> getStudentsByUserCode(String userCode, String centerCode) {
        System.out.println(userCode);
        List<MainStudentDTO> rows = studentRepository.selectStudentByUserCode(userCode, centerCode);
        return rows;
    }

    // 개별 학생 조회
    public StudentWebRespDTO.StudentDTO getStudentDetailByStudentId(String studentId) {

        StudentWebRespDTO.StudentInfoDTO student = studentRepository.findStudentInfoByStudentId(studentId);
        List<GradeCode> grades = gradeJpaRepository.findAll();
        StudentWebRespDTO.StudentPaymentDTO payment = studentRepository.findStudentPaymentByStudentId(studentId);
//        studentRepository.findStudentAttendanceByStudentId(studentId);
//        studentRepository.findStudentConsultByStudentId(studentId);

        StudentWebRespDTO.StudentDTO studentDetailRespDTO = StudentWebRespDTO.StudentDTO.builder()
                .studentInfo(student)
                .studentPayment(payment)
                .gradeCodes(grades)
                .build();
        return studentDetailRespDTO;
    }

    public String studentInsert(StudentWebReqDTO.StudentJoinDTO studentDTO, StudentWebReqDTO.ParentJoinDTO parentDTO) {
        String today = dateConfig.currentYearMonth().get("today");
        String random = UUID.randomUUID().toString().replace("-", "");
        String last5 = random.substring(random.length() - 5).toUpperCase();
        String code = studentDTO.getCenterCode() + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + last5;

        studentDTO.setStudentId(code);

        if (studentDTO.getBirth() != null) {
            String formattedBirth = convertBirthToFullDate(studentDTO.getBirth());
            studentDTO.setBirth(formattedBirth);
        }

        String phoneNumber = parentDTO.getParentTelMiddle() + parentDTO.getParentTelLast();
        Integer maxSuffix = studentRepository.findMaxAppIdSuffix(phoneNumber);

        int nextSuffix = (maxSuffix == null) ? 0 : maxSuffix + 1;

        String appId = phoneNumber + nextSuffix;

        studentDTO.setAppId(appId);
        studentDTO.setAppPassword(
                DigestUtils.sha256Hex(parentDTO.getParentTelLast())
        );
        studentDTO.setEntryHanDate(today);

        studentRepository.insert(studentDTO);
        parentDTO.setStudentId(studentDTO.getStudentId());
        parentDTO.setSignature(studentDTO.getStudentId() + "signature.png");
        familyService.parentInsert(parentDTO);

        return studentDTO.getStudentId();
    }

    private String convertBirthToFullDate(String birth) {
        if (birth == null || birth.length() != 6) return null;

        String yy = birth.substring(0, 2);
        String mm = birth.substring(2, 4);
        String dd = birth.substring(4, 6);

        // 현재 연도 기준 세기 판정
        int year2 = Integer.parseInt(yy);
        int currentYY = LocalDate.now().getYear() % 100;
        int fullYear = (year2 > currentYY) ? 1900 + year2 : 2000 + year2;

        return String.format("%04d-%02d-%02d", fullYear, Integer.parseInt(mm), Integer.parseInt(dd));
    }

    public List<GradeCode> findGrade() {

        List<GradeCode> gradeCodes = gradeJpaRepository.findAll();
        return gradeCodes;
    }

    public List<RelationCode> findRelation() {

        List<RelationCode> relationCodes = relationJpaRepository.findAll();
        return relationCodes;
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
        String today = dateConfig.currentYearMonth().get("today");
        historyDTO.setUserCode(userCode);
        if (historyDTO.getStatusKey().equals("ACTIVE")) {
            historyDTO.setReason(today + " 재원중으로 변경");
        }

        int updateResult = studentRepository.studentStatusUpdate(historyDTO);
        System.out.println(updateResult);
        if (updateResult == 0) {
            System.out.println("업데이트 실패");
        } else {
            System.out.println("업데이트 성공");
        }


        int insertResult = studentRepository.statusHistoryInsert(historyDTO);
        if (insertResult == 0) {
            System.out.println("인서트 실패");
        } else {
            System.out.println("인서트 성공");
        }

        StudentWebRespDTO.StudentStatusDTO respDTO = studentRepository.findStatusByStudentId(historyDTO.getStudentId());

        return respDTO;
    }


    public int updateStudentInfo(StudentWebReqDTO.StudentUpdateDTO req) {

        studentRepository.updateStudentInfo(req);

        String rawPhone = req.getParentPhone();
        String first = rawPhone.substring(0, 3);
        String middle = rawPhone.substring(3, 7);
        String last = rawPhone.substring(7, 11);
        studentRepository.updateParent(first, middle, last, req.getRelationKey(), req.getStudentId());

        insertTeacherAssign(req);

        return 1;
    }

    public int updatePaymentInfo(StudentWebReqDTO.StudentPaymentUpdateDTO dto) {
        studentRepository.updateStudentPayment(dto);

        return 1;

    }

    public void updateCourseStatus(StudentWebReqDTO.StudentCourseUpdateDTO request) {
        log.info("수강상태 수정 처리: studentId={}, hanChanged={}, bookChanged={}",
                request.getStudentId(), request.getHanChanged(), request.getBookChanged());

        // 한자 수강상태 변경
        if (Boolean.TRUE.equals(request.getHanChanged())) {
            if (request.getHanState() == 1) {
                // 수강으로 변경
                log.info("한자 수강으로 변경: entryHanDate={}", request.getEntryHanDate());
                studentRepository.updateHanToActive(
                        request.getStudentId(),
                        request.getEntryHanDate()
                );
            } else {
                // 미수강으로 변경
                log.info("한자 미수강으로 변경: inactiveDate={}, reason={}",
                        request.getInactiveHanDate(), request.getInactiveHanReason());
                studentRepository.updateHanToInactive(
                        request.getStudentId(),
                        request.getInactiveHanDate(),
                        request.getInactiveHanReason()
                );
            }
        }

        // 독서 수강상태 변경
        if (Boolean.TRUE.equals(request.getBookChanged())) {
            if (request.getBookState() == 1) {
                // 수강으로 변경
                log.info("독서 수강으로 변경: entryBookDate={}", request.getEntryBookDate());
                studentRepository.updateBookToActive(
                        request.getStudentId(),
                        request.getEntryBookDate()
                );
            } else {
                // 미수강으로 변경
                log.info("독서 미수강으로 변경: inactiveDate={}, reason={}",
                        request.getInactiveBookDate(), request.getInactiveBookReason());
                studentRepository.updateBookToInactive(
                        request.getStudentId(),
                        request.getInactiveBookDate(),
                        request.getInactiveBookReason()
                );
            }
        }

    }

    public void insertTeacherAssign(StudentWebReqDTO.StudentUpdateDTO req) {

        TeacherAssign old = studentRepository.findTeacherAssign(req.getStudentId());

        boolean newHan = req.getEntryHanDate() != null;
        boolean newBook = req.getEntryBookDate() != null;
        if (old == null) {

            TeacherAssign create = TeacherAssign.builder()
                    .student(Student.builder().studentId(req.getStudentId()).build())
                    .entryHanDate(req.getEntryHanDate())
                    .entryBookDate(req.getEntryBookDate())
                    .assignHanTeacher(newHan ? User.builder().userCode(req.getUserCode()).build() : null)
                    .assignBookTeacher(newBook ? User.builder().userCode(req.getUserCode()).build() : null)
                    .build();

            studentRepository.insertTeacherAssign(create);
        }

    }

    public void assignTeacher(String studentId, ClassRespDTO.BasicTimeTableInfo info) {

        if (info == null) return;

        TeacherAssign assign = studentRepository.findTeacherAssign(studentId);
        String today = LocalDate.now().toString();

        boolean isHan = "1".equals(info.getClassType());
        boolean isBook = "2".equals(info.getClassType());

        User teacher = User.builder().userCode(info.getTeacherCode()).build();
        ClassCode classCode = ClassCode.builder().classKey(info.getClassKey()).build();

        if (assign == null) {
            TeacherAssign newAssign = TeacherAssign.builder()
                    .student(Student.builder().studentId(studentId).build())
                    .hanState(isHan)
                    .bookState(isBook)
                    .assignHanTeacher(isHan ? teacher : null)
                    .assignBookTeacher(isBook ? teacher : null)
                    .assignHanClass(isHan ? classCode : null)
                    .assignBookClass(isBook ? classCode : null)
                    .entryHanDate(isHan ? today : null)
                    .entryBookDate(isBook ? today : null)
                    .build();

            studentRepository.insertTeacherAssign(newAssign);
            return;
        }

        boolean oldHan = assign.getHanState() != null && assign.getHanState();
        boolean oldBook = assign.getBookState() != null && assign.getBookState();


        if (isHan == oldHan && isBook == oldBook) {
            return;
        }

        ClassReqDTO.TeacherAssignUpdateDTO dto = new ClassReqDTO.TeacherAssignUpdateDTO();
        dto.setStudentId(studentId);

        dto.setHanState(oldHan || isHan);
        dto.setBookState(oldBook || isBook);

        if (isHan) {
            dto.setHanTeacher(info.getTeacherCode());
            dto.setHanClass(info.getClassKey());
            if (!oldHan) {
                dto.setHanEntryDate(today);
            }
        }

        if (isBook) {
            dto.setBookTeacher(info.getTeacherCode());
            dto.setBookClass(info.getClassKey());
            if (!oldBook) {
                dto.setBookEntryDate(today);
            }
        }

        studentRepository.updateTeacherAssign(dto);
    }


//    public String insertStudentClass(ClassRespDTO.ClassInfoDTO dto, String studentId, String yy, String mm) {
//        Integer fee = paymentRepository.findFeeByClassKey(dto.getClassKey(), dto.getCenterCode());
//
//        StudentClass.StudentClassBuilder builder = StudentClass.builder()
//                .student(Student.builder().studentId(studentId).build())
//                .yy(yy)
//                .mm(mm);
//
//        if ("1".equals(dto.getClassType())) {
//            builder
//                    .hanClassCode(ClassCode.builder().classKey(dto.getClassKey()).build())
//                    .hanUser(User.builder().userCode(dto.getUserCode()).build())
//                    .hanFee(fee);
//
//        } else if ("2".equals(dto.getClassType())) {
//            builder
//                    .bookClassCode(ClassCode.builder().classKey(dto.getClassKey()).build())
//                    .bookUser(User.builder().userCode(dto.getUserCode()).build())
//                    .bookFee(fee);
//        }
//
//        StudentClass studentClass = builder.build();
//
//        StudentClass existing = studentRepository.findStudentClassByStudentId(studentId, yy, mm);
//
//        if (existing != null)
//            studentRepository.updateStudentClass(studentClass);
//        else
//            studentRepository.insertStudentClass(studentClass);
//
//        return "ok";
//    }

    public List<StudentTransferDTO> findInOutByStudentId(Integer studentId) {
        List<StudentTransferDTO> responseDTO = studentRepository.findInOutByStudentId(studentId);
        return responseDTO;
    }

    @Transactional
    public void transferStudent(StudentWebReqDTO.StudentTransferDTO reqDto, String userCode) {

        String[] parts = reqDto.getMoveAt().split("-");
        String yy = parts[0];
        String mm = parts[1];

        // 한자 과목 선택
        if (reqDto.getSelectedHan() != null) {
            for (String studentId : reqDto.getStudents()) {

                StudentWebRespDTO.TeacherDTO teacher = studentRepository.findTeacherAssignByStudentId(studentId);
                if (teacher.getAssignHanTeacher() == null) {
                    throw new Exception400("등록된 한자 수업이 없습니다.");
                }

                // 전입/전출 업데이트
                studentRepository.updateTransfer(studentId, reqDto.getUserCode(), reqDto.getSelectedHan(), yy, mm);

                // 시간표 삭제
                // 시간표 등록 전에 할 수도 있잖아?
                StudentWebRespDTO.TransferTimeTableInfoDTO dto = classRepository.findTimeTableKeyByStudentId(studentId, reqDto.getSelectedHan(), yy, mm);

                if (dto != null) {
                    classRepository.deleteByKeyAndStudentId(dto.getTimeTableKey(), studentId);
                    paymentService.deleteDetail(dto.getTimeTableKey(), studentId);
                }

                // 전입 전출 히스토리 저장
                StudentTransferHistory history = StudentTransferHistory.builder()
                        .student(Student.builder().studentId(studentId).build())
                        .fromUser(User.builder().userCode(teacher.getAssignHanTeacher()).build())
                        .toUser(User.builder().userCode(reqDto.getUserCode()).build())
                        .classCode(ClassCode.builder().classKey(teacher.getAssignHanClass()).build())
                        .classType(reqDto.getSelectedHan())
                        .transferReason(reqDto.getTransferReason())
                        .updatedBy(userCode)
                        .moveAt(reqDto.getMoveAt())
                        .build();

                studentRepository.insertTransferHistory(history);

            }
        }
        // 독서 과목 선택
        if (reqDto.getSelectedBook() != null) {
            for (String studentId : reqDto.getStudents()) {

                StudentWebRespDTO.TeacherDTO teacher = studentRepository.findTeacherAssignByStudentId(studentId);
                if (teacher.getAssignBookTeacher() == null) {
                    throw new Exception400("등록된 독서 수업이 없습니다.");
                }
                // 전입/전출
                studentRepository.updateTransfer(studentId, reqDto.getUserCode(), reqDto.getSelectedBook(), yy, mm);

                // 시간표 삭제
                StudentWebRespDTO.TransferTimeTableInfoDTO dto = classRepository.findTimeTableKeyByStudentId(studentId, reqDto.getSelectedBook(), yy, mm);

                if (dto != null) {
                    classRepository.deleteByKeyAndStudentId(dto.getTimeTableKey(), studentId);
                    paymentService.deleteDetail(dto.getTimeTableKey(), studentId);
                }

                // history 저장
                StudentTransferHistory history = StudentTransferHistory.builder()
                        .student(Student.builder().studentId(studentId).build())
                        .fromUser(User.builder().userCode(teacher.getAssignBookTeacher()).build())
                        .toUser(User.builder().userCode(reqDto.getUserCode()).build())
                        .classCode(ClassCode.builder().classKey(teacher.getAssignBookClass()).build())
                        .classType(reqDto.getSelectedBook())
                        .transferReason(reqDto.getTransferReason())
                        .updatedBy(userCode)
                        .moveAt(reqDto.getMoveAt())
                        .build();

                studentRepository.insertTransferHistory(history);

            }
        }

    }

    public StudentAppRespDTO.AppTokenRespDTO findAppTokenByAppId(String appId) {

        StudentAppRespDTO.AppTokenRespDTO respDTO = studentRepository.findAppTokenByAppId(appId);
        return respDTO;
    }

    public String checkinStudent(StudentAppReqDTO.StudentAttendanceDTO dto, Student student) {

        if (dto == null || student == null) {
            throw new RuntimeException("잘못된 요청 데이터입니다.");
        }

        // 먼저 해당 날짜에 이미 등원 처리된 기록이 있는지 확인
        List<StudentAttendance> existingAttendance = studentRepository.findByStudentAndDate(
                student.getStudentId(),
                dto.getYmd()
        );

        if (existingAttendance != null && !existingAttendance.isEmpty()) {

            boolean alreadyCheckedIn = existingAttendance.stream()
                    .anyMatch(att -> att.getInTime() != null);

            if (alreadyCheckedIn) {
                return "7777";
            }
        }

        LocalDate today = LocalDate.parse(dto.getYmd());
        String year = String.valueOf(today.getYear());
        String month = String.format("%02d", today.getMonthValue());
        String todayDayname = getDayname(today.getDayOfWeek());

        List<ClassRespDTO.TimeRangeDTO> timeDTO = studentRepository.getStartClassTime(student.getStudentId(), year, month);
        if (timeDTO == null || timeDTO.isEmpty()) {
            throw new RuntimeException("수업 시간이 설정되지 않았습니다.");
        }

        List<ClassRespDTO.TimeRangeDTO> todayClasses = timeDTO.stream()
                .filter(t -> todayDayname.equals(t.getDayname()))
                .collect(Collectors.toList());

        if (todayClasses.isEmpty()) {
            throw new RuntimeException("오늘은 수업이 없는 날입니다.");
        }

        LocalTime checkInTime = LocalTime.parse(dto.getHhmm());

        List<ClassWeek> weekList = classRepository.findClassWeekByCenter(dto.getCenterCode(), year, month);
        if (weekList == null || weekList.isEmpty()) {
            throw new RuntimeException("이번 달 주차 설정이 없습니다.");
        }

        String week = findWeekByDate(today, weekList);
        if (week == null) {
            throw new RuntimeException("오늘 날짜는 어떤 주차에도 속하지 않습니다.");
        }

        int totalUpdated = 0;

        for (ClassRespDTO.TimeRangeDTO targetClass : todayClasses) {

            LocalTime start = LocalTime.parse(targetClass.getStartTime());

            String attendanceKey = checkInTime.isAfter(start) ? "late" : "present";

            int updated = studentRepository.checkinStudentAttendance(
                    student.getStudentId(),
                    dto.getYmd(),
                    dto.getHhmm(),
                    targetClass.getEndTime(),
                    attendanceKey,
                    week,
                    year,
                    month,
                    targetClass.getTimeTableKey()
            );

            if (updated > 0) {
                totalUpdated += updated;
            }
        }

        return "0000";
    }

    public String checkoutStudent(StudentAppReqDTO.StudentAttendanceDTO dto, Student student) {

        if (dto == null || student == null) {
            throw new RuntimeException("잘못된 요청 데이터입니다.");
        }

        // 오늘 날짜의 모든 출석 기록 조회
        List<StudentAttendance> attendanceList = studentRepository.findByStudentAndDate(
                student.getStudentId(),
                dto.getYmd()
        );

        if (attendanceList == null || attendanceList.isEmpty()) {
            System.out.println("등원 기록 없음");
            return "8888";
        }

        // 이미 하원 처리된 교시가 있는지 확인
        boolean alreadyCheckedOut = attendanceList.stream()
                .anyMatch(attendance -> attendance.getOutTime() != null);

        if (alreadyCheckedOut) {
            return "6666"; // 이미 하원 처리됨
        }

        // 해당 날짜의 모든 교시 하원 처리
        int totalUpdated = studentRepository.checkoutStudentAttendance(
                student.getStudentId(),
                dto.getHhmm(),
                dto.getYmd()
        );

        if (totalUpdated == 0) {
            throw new RuntimeException("하원 처리에 실패했습니다.");
        }

        System.out.println("총 " + totalUpdated + "개 교시 하원 완료");
        return "0000";
    }

    private String getDayname(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "mon";
            case TUESDAY -> "tue";
            case WEDNESDAY -> "wed";
            case THURSDAY -> "thu";
            case FRIDAY -> "fri";
            case SATURDAY -> "sat";
            case SUNDAY -> "sun";
        };
    }

    private LocalDateTime endOfMonthDateTime(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4));
        int m = Integer.parseInt(ym.substring(4, 6));
        LocalDate end = YearMonth.of(y, m).atEndOfMonth();
        return end.atTime(23, 59, 59);
    }

    private String findWeekByDate(LocalDate today, List<ClassWeek> weekList) {
        for (ClassWeek w : weekList) {
            if (isSameDate(today, w.getMon()) ||
                    isSameDate(today, w.getTue()) ||
                    isSameDate(today, w.getWed()) ||
                    isSameDate(today, w.getThu()) ||
                    isSameDate(today, w.getFri()) ||
                    isSameDate(today, w.getSat()) ||
                    isSameDate(today, w.getSun())) {

                return w.getWeek();
            }
        }
        return null;
    }

    private boolean isSameDate(LocalDate today, String target) {
        if (target == null || target.isBlank()) {
            return false;
        }
        try {
            return today.isEqual(LocalDate.parse(target));
        } catch (Exception e) {
            return false;
        }
    }

    //========================================================================================

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

        if (row == null || !password.equals(row.getAppPassword())) {
            throw new AppRestfulException("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.FORBIDDEN);
        }

        // 형제 정보 조회
        String siblingKey = studentRepository.findSiblingKeyByStudentId(row.getStudentId());
        String brotherGb = siblingKey != null ? "Y" : "N";

        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        String ihak = studentRepository.getBookCodeByClassType(
                row.getStudentId(),
                year,
                month
        );


        StudentAppRespDTO.AppLoginRespDTO respDTO = StudentAppRespDTO.AppLoginRespDTO.builder()
                .stuid(row.getStudentId())
                .name(row.getStudentName())
                .cid(row.getCenterCode())
                .brotherGb(brotherGb)
                .sibling(siblingKey)
                .cname(row.getCenterName())
                .hak(row.getGradeKey())
                .ihak(ihak)
                .profileimg("1")
                .appid(row.getAppId())
                .build();

        return respDTO;
    }


    public StudentAppRespDTO.AppLoginRespDTO loginSkip(String appId) {
        StudentAppRespDTO.AppLoginViewDTO row = studentRepository.appLogin(appId);


        // 형제 정보 조회
        String siblingKey = studentRepository.findSiblingKeyByStudentId(row.getStudentId());
        String brotherGb = siblingKey != null ? "Y" : "N";

        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        String ihak = studentRepository.getBookCodeByClassType(
                row.getStudentId(),
                year,
                month
        );


        StudentAppRespDTO.AppLoginRespDTO respDTO = StudentAppRespDTO.AppLoginRespDTO.builder()
                .stuid(row.getStudentId())
                .name(row.getStudentName())
                .cid(row.getCenterCode())
                .brotherGb(brotherGb)
                .sibling(siblingKey)
                .cname(row.getCenterName())
                .hak(row.getGradeKey())
                .ihak(ihak)
                .profileimg("1")
                .appid(row.getAppId())
                .build();

        return respDTO;
    }

    public List<StudentAppRespDTO.AttendanceListRespDTO> findAttendanceList(StudentAppReqDTO.AttendanceListReqDTO dto) {
        String yy = dto.getYm().substring(0, 4);
        String mm = dto.getYm().substring(4, 6);
        List<StudentAppRespDTO.AttendanceListRespDTO> attendanceList =
                studentRepository.findAttendanceList(dto.getId(), yy, mm);
        attendanceList.stream()
                .peek(schedule -> {
                    schedule.setDayname(convertDaynameToKorean(schedule.getDayname()));

                    // 실제 시간 포맷팅 (콜론 제거)
                    if (schedule.getStime() != null && !schedule.getStime().isEmpty()) {
                        schedule.setStime(schedule.getStime().replace(":", ""));
                    }
                    if (schedule.getEtime() != null && !schedule.getEtime().isEmpty()) {
                        schedule.setEtime(schedule.getEtime().replace(":", ""));
                    }

                    // 예정 시간 포맷팅 (콜론 제거)
                    if (schedule.getPlannedStime() != null && !schedule.getPlannedStime().isEmpty()) {
                        schedule.setPlannedStime(schedule.getPlannedStime().replace(":", ""));
                    }
                    if (schedule.getPlannedEtime() != null && !schedule.getPlannedEtime().isEmpty()) {
                        schedule.setPlannedEtime(schedule.getPlannedEtime().replace(":", ""));
                    }
                })
                .toList();
        return attendanceList;
    }

    private String convertDaynameToKorean(String dayname) {
        return switch (dayname.toLowerCase()) {
            case "mon" -> "월요일";
            case "tue" -> "화요일";
            case "wed" -> "수요일";
            case "thu" -> "목요일";
            case "fri" -> "금요일";
            case "sat" -> "토요일";
            case "sun" -> "일요일";
            default -> dayname;
        };
    }

    public List<StudentAppRespDTO.AttendanceMainRespDTO> findAttendanceMain(StudentAppReqDTO.StudentAttendanceMainDTO dto) {
        return studentRepository.findAttendanceMain(dto.getId());
    }


    public List<StudentAppRespDTO.AppSiblingRespDTO> findSibling(StudentAppReqDTO.SiblingReqDTO reqDTO) {

        List<StudentAppRespDTO.AppSiblingRespDTO> siblings =
                studentRepository.findSiblingBySiblingKey(reqDTO.getSibling());

        // 각 형제의 ihak(book_code) 조회
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        for (StudentAppRespDTO.AppSiblingRespDTO sibling : siblings) {
            String ihak = studentRepository.getBookCodeByClassType(
                    sibling.getStuid(),
                    year,
                    month
            );
            sibling.setIhak(ihak);
        }

        return siblings;
    }
}
