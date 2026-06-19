package com.hohoedu.all_pass.student;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.handler.exception.AppRestfulException;
import com.hohoedu.all_pass._core.handler.exception.CustomRestfulException;
import com.hohoedu.all_pass._core.handler.exception.DuplicateStudentException;
import com.hohoedu.all_pass._core.handler.exception.Exception400;
import com.hohoedu.all_pass._core.utils.KeyGenerator;
import com.hohoedu.all_pass.attendance.AttendanceRepository;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.repository.CenterJpaRepository;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassWeek;
import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import com.hohoedu.all_pass.family.FamilyService;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.popbill.PopbillService;
import com.hohoedu.all_pass.student._dto.app.StudentAppReqDTO;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student.model.*;
import com.hohoedu.all_pass.student.repository.*;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
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
    private final SnapshotRepository snapshotRepository;
    private final SnapshotJpaRepository snapshotJpaRepository;
    private final CenterJpaRepository centerRepository;
    private final FamilyService familyService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final AttendanceRepository attendanceRepository;
    private final PopbillService popbillService;

    public StudentWebRespDTO.MainStudentStatusDTO getStudentStatus(
            String centerCode, String userCode, String roleKey, String year, String month) {
        return studentRepository.findStudentStatus(centerCode, userCode, year, month, roleKey);
    }

    public List<StudentWebRespDTO.StudentsListDTO> findStudentByCenterCode(String year, String month, String centerCode,
                                                                           String userCode) {

        List<StudentWebRespDTO.StudentsListDTO> student = studentRepository.findStudentByCenterCode(year, month,
                centerCode, userCode);

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
    public StudentWebRespDTO.StudentDTO getStudentDetailByStudentId(String studentId, String centerCode) {
        String year = String.valueOf(org.threeten.bp.LocalDate.now().getYear());
        String month = String.format("%02d", org.threeten.bp.LocalDate.now().getMonthValue());
        StudentWebRespDTO.StudentInfoDTO student = studentRepository.findStudentInfoByStudentId(studentId);
        List<GradeCode> grades = gradeJpaRepository.findAll();
        StudentWebRespDTO.StudentPaymentDTO payment = studentRepository.findStudentPaymentByStudentId(studentId);
        List<StudentWebRespDTO.StudentAttendanceDTO> attendance = studentRepository
                .findStudentAttendanceByStudentId(studentId, year, month);
        List<StudentWebRespDTO.StudentConsultDTO> consult = studentRepository.findStudentConsultByStudentId(studentId);
        List<StudentWebRespDTO.HanClass> hanClasses = studentRepository.findHanClasses(centerCode, studentId);
        List<StudentWebRespDTO.BookClass> bookClasses = studentRepository.findBookClasses(centerCode, studentId);
        List<StudentWebRespDTO.HanTeacher> hanTeachers = studentRepository.findHanTeachers(centerCode);
        List<StudentWebRespDTO.BookTeacher> bookTeachers = studentRepository.findBookTeachers(centerCode);
        StudentWebRespDTO.StudentDTO studentDetailRespDTO = StudentWebRespDTO.StudentDTO.builder()
                .studentInfo(student)
                .studentPayment(payment)
                .gradeCodes(grades)
                .studentAttendance(attendance)
                .studentCounsult(consult)
                .hanClasses(hanClasses)
                .bookClasses(bookClasses)
                .hanTeachers(hanTeachers)
                .bookTeachers(bookTeachers)
                .build();
        log.info(studentDetailRespDTO.toString());
        return studentDetailRespDTO;
    }

    public void createPendingStudent(PendingStudent pendingStudent) {
        studentRepository.createPendingStudent(pendingStudent);
    }

    public StudentWebRespDTO.PendingStudentRespDTO findStudentByInviteCode(String inviteCode) {

        return studentRepository.findStudentByInviteCode(inviteCode);
    }

    public List<StudentWebRespDTO.StudentAttendanceDTO> getAttendanceByMonth(String studentId, String yy, String mm) {
        return studentRepository.findStudentAttendanceByStudentId(studentId, yy, mm);
    }

    @Transactional
    public String studentInsert(StudentWebReqDTO.StudentJoinDTO studentDTO, StudentWebReqDTO.ParentJoinDTO parentDTO) {
        int dupStudent = studentRepository.checkDuplicateStudent(studentDTO.getStudentName(),
                parentDTO.getParentTelMiddle(), parentDTO.getParentTelLast());
        if (dupStudent > 0) {
            throw new DuplicateStudentException("이미 등록된 학생입니다.");
        }

        String today = DateConfig.currentYearMonth().get("today");
        String random = UUID.randomUUID().toString().replace("-", "");
        String last5 = random.substring(random.length() - 5).toUpperCase();
        String code = studentDTO.getCenterCode() + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + last5;

        studentDTO.setStudentId(code);

        if (studentDTO.getBirth() != null) {
            String formattedBirth = convertBirthToFullDate(studentDTO.getBirth());
            studentDTO.setBirth(formattedBirth);
        }
        String billingPhone = parentDTO.getParentTelFirst() + parentDTO.getParentTelMiddle()
                + parentDTO.getParentTelLast();

        String phoneNumber = parentDTO.getParentTelMiddle() + parentDTO.getParentTelLast();

        Integer maxSuffix = studentRepository.findMaxAppIdSuffix(phoneNumber);

        int nextSuffix = (maxSuffix == null) ? 0 : maxSuffix + 1;

        String appId = phoneNumber + nextSuffix;

        studentDTO.setAppId(appId);
        studentDTO.setAppPassword(DigestUtils.sha256Hex(parentDTO.getParentTelLast()));
        studentDTO.setEntryHanDate(today);
        studentDTO.setBillingPhone(billingPhone);

        switch (studentDTO.getSubject()) {
            case "hoho" -> {
                studentDTO.setSubHoho(true);
                studentDTO.setSubHan(false);
                studentDTO.setSubBook(false);
            }
            case "han" -> {
                studentDTO.setSubHoho(false);
                studentDTO.setSubHan(true);
                studentDTO.setSubBook(true);
            }
            case "book" -> {
                studentDTO.setSubHoho(false);
                studentDTO.setSubHan(false);
                studentDTO.setSubBook(true);
            }
        }

        studentRepository.insert(studentDTO);
        parentDTO.setStudentId(studentDTO.getStudentId());
        parentDTO.setSignature(studentDTO.getStudentId() + "signature.png");
        familyService.parentInsert(parentDTO);
        familyService.processSibling(
                studentDTO.getStudentId(),
                studentDTO.getCenterCode(),
                parentDTO.getParentTelFirst(),
                parentDTO.getParentTelMiddle(),
                parentDTO.getParentTelLast());

        String inviteCode = studentDTO.getInviteCode();
        if (inviteCode != null && !inviteCode.isEmpty()) {
            log.info("inviteCode : {}", inviteCode);
            processInviteCompletion(inviteCode, studentDTO);
        } else {

            PendingStudent newPending = PendingStudent.builder()
                    .studentId(studentDTO.getStudentId())
                    .name(studentDTO.getStudentName())
                    .phone(parentDTO.getParentTelFirst() + parentDTO.getParentTelMiddle()
                            + parentDTO.getParentTelLast())
                    .sendKey(KeyGenerator.generateSendKey())
                    .gradeKey(studentDTO.getGradeKey())
                    .userCode(studentDTO.getUserCode())
                    .centerCode(studentDTO.getCenterCode())
                    .status("REGISTERED")
                    .subHoho(studentDTO.isSubHoho())
                    .subHan(studentDTO.isSubHan())
                    .subBook(studentDTO.isSubBook())
                    .build();

            studentRepository.insertPendingStudent(newPending);
        }

        studentRepository.createTeacherAssign(studentDTO.getStudentId(), studentDTO.getCenterCode());

        return studentDTO.getStudentId();
    }

    private void processInviteCompletion(String inviteCode, StudentWebReqDTO.StudentJoinDTO studentDTO) {
        try {
            InviteTracking invite = studentRepository.findByInviteCode(inviteCode);

            String userPhone = userRepository.findUserPhoneByUserCode(invite.getUserCode());

            if (userPhone != null && !userPhone.isEmpty()) {
                popbillService.sendJoinCompletionAlimtalk(
                        invite.getCenterCode(),
                        userPhone,
                        invite.getReceiverPhone(),
                        invite.getUserCode(),
                        studentDTO);
            }

            PendingStudent pending = studentRepository.findPendingStudentBySendKey(invite.getSendKey());

            if (pending != null) {
                studentRepository.updatePendingStudentOnRegister(
                        invite.getSendKey(),
                        studentDTO.getStudentId(),
                        studentDTO.getStudentName(),
                        studentDTO.getGradeKey());
            } else {
                PendingStudent newPending = PendingStudent.builder()
                        .name(studentDTO.getStudentName())
                        .phone(invite.getReceiverPhone())
                        .sendKey(invite.getSendKey() != null ? invite.getSendKey() : KeyGenerator.generateSendKey())
                        .inviteCode(inviteCode)
                        .userCode(invite.getUserCode())
                        .centerCode(invite.getCenterCode())
                        .status("REGISTERED")
                        .subHoho(studentDTO.isSubHoho())
                        .subHan(studentDTO.isSubHan())
                        .subBook(studentDTO.isSubBook())
                        .studentId(studentDTO.getStudentId())
                        .build();
                studentRepository.createPendingStudent(newPending);
            }

        } catch (Exception e) {
            log.error("초대 완료 알림톡 발송 실패 - inviteCode: {}, error: {}",
                    inviteCode, e.getMessage(), e);
        }
    }

    private String convertBirthToFullDate(String birth) {
        if (birth == null || birth.length() != 6)
            return null;

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

    public List<StudentInOutDTO> findAllInOut(String centerCode, String userCode) {

        List<StudentInOutDTO> students = studentRepository.selectTransferStudents(centerCode, userCode);

        return students;
    }

    public List<User> findTeacher(UserRespDTO.LoginRespDTO dto) {
        List<User> users = userRepository.findUserByCenterCode(dto.getCenterCode(), dto.getRoleNum(), dto.getType(),
                dto.getUserCode());
        return users;
    }

    public StudentWebRespDTO.StudentStatusDTO statusInsert(StudentWebReqDTO.StatusHistoryDTO historyDTO,
                                                           String userCode) {
        String today = DateConfig.currentYearMonth().get("today");
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

    @Transactional
    public int updateStudentInfo(StudentWebReqDTO.StudentUpdateDTO req) {

        String rawPhone = req.getParentPhone();
        String first = rawPhone.substring(0, 3);
        String middle = rawPhone.substring(3, 7);
        String last = rawPhone.substring(7, 11);

        String baseAppId = middle + last;
        String appId = baseAppId + 0;
        int suffix = 0;

        while (studentRepository.existsByAppIdAndNotStudentId(appId, req.getStudentId())) {
            suffix++;
            appId = baseAppId + suffix;
        }

        req.setAppId(appId);

        studentRepository.updateStudentInfo(req);

        studentRepository.updateParent(first, middle, last, req.getRelationKey(), req.getStudentId());

        insertTeacherAssign(req);

        return 1;
    }

    @Transactional
    public int updatePaymentInfo(StudentWebReqDTO.StudentPaymentUpdateDTO dto) {
        // 학생 정보의 교재비 수정
        studentRepository.updateStudentPayment(dto);
        String paymentKey = paymentRepository.findLatestPaymentKeyByStudentId(dto.getStudentId());
        dto.setPaymentKey(paymentKey);
        if (dto.getHanMaterialFee() != null) {
            paymentRepository.updatePaymentDetailBookFee(
                    dto.getStudentId(),
                    "1", // 한자
                    dto.getHanMaterialFee(),
                    dto.getPaymentKey());
        }

        // 3. payment_detail BOOK_FEE 수정 - 독서
        if (dto.getBookMaterialFee() != null) {
            paymentRepository.updatePaymentDetailBookFee(
                    dto.getStudentId(),
                    "2", // 독서
                    dto.getBookMaterialFee(),
                    dto.getPaymentKey());
        }
        return 1;
    }

    @Transactional
    public void updateCourseStatus(StudentWebReqDTO.StudentCourseUpdateDTO request, String userCode) {
        StudentWebRespDTO.TeacherDTO teacherDTO = studentRepository
                .findTeacherAssignByStudentId(request.getStudentId());

        if (teacherDTO == null) {
            throw new Exception400("학생 정보를 찾을 수 없습니다.");
        }

        // 로그용 스냅샷 조회 (변경 전 상태 저장용)
        StudentWebRespDTO.StudentSnapshotDTO snapshot = studentRepository
                .findSnapshotByStudentId(request.getStudentId());

        String hanTeacher = request.getHanTeacherCode() != null
                ? request.getHanTeacherCode()
                : teacherDTO.getAssignHanTeacher();

        String bookTeacher = request.getBookTeacherCode() != null
                ? request.getBookTeacherCode()
                : teacherDTO.getAssignBookTeacher();

        // 한자 상태 업데이트
        if (Boolean.TRUE.equals(request.getHanChanged())) {
            if (request.getHanState() == 1) {
                studentRepository.updateHanToActive(
                        request.getStudentId(),
                        request.getEntryHanDate(),
                        hanTeacher);
            } else {
                // 미수강 전환 시 로그 저장
                studentRepository.insertWithdrawLog(
                        request.getStudentId(),
                        "han",
                        snapshot,
                        request.getInactiveHanReason(),
                        request.getInactiveHanDate(),
                        userCode);
                studentRepository.updateHanToInactive(
                        request.getStudentId(),
                        request.getInactiveHanDate(),
                        request.getInactiveHanReason(),
                        teacherDTO.getAssignHanTeacher());
            }
        }

        // 독서 상태 업데이트
        if (Boolean.TRUE.equals(request.getBookChanged())) {
            if (request.getBookState() == 1) {
                studentRepository.updateBookToActive(
                        request.getStudentId(),
                        request.getEntryBookDate(),
                        bookTeacher);
            } else {
                // 미수강 전환 시 로그 저장
                studentRepository.insertWithdrawLog(
                        request.getStudentId(),
                        "book",
                        snapshot,
                        request.getInactiveBookReason(),
                        request.getInactiveBookDate(),
                        userCode);
                studentRepository.updateBookToInactive(
                        request.getStudentId(),
                        request.getInactiveBookDate(),
                        request.getInactiveBookReason(),
                        teacherDTO.getAssignBookTeacher());
            }
        }

        if (request.getHanState() == 1
                && (request.getHanClassKey() != null || request.getHanTeacherCode() != null)) {
            studentRepository.updateHanClassAndTeacher(
                    request.getStudentId(),
                    request.getHanClassKey(),
                    request.getHanTeacherCode());
        }

        if (request.getBookState() == 1
                && (request.getBookClassKey() != null || request.getBookTeacherCode() != null)) {
            studentRepository.updateBookClassAndTeacher(
                    request.getStudentId(),
                    request.getBookClassKey(),
                    request.getBookTeacherCode());
        }

        if (request.getHanState() == 1 || request.getBookState() == 1) {
            studentRepository.updatePendingIsDeletedByStudentId(request.getStudentId());
            studentRepository.updateStudentStatusToActive(request.getStudentId());
        }
    }

    @Transactional
    public void restoreStudent(String studentId, String userCode) {

        // 한자/독서 탈퇴 로그 각각 조회
        StudentWebRespDTO.WithdrawLogDTO hanLog = studentRepository.findLatestWithdrawLog(studentId, "han");
        StudentWebRespDTO.WithdrawLogDTO bookLog = studentRepository.findLatestWithdrawLog(studentId, "book");

        if (hanLog == null && bookLog == null) {
            throw new Exception400("탈퇴 이력을 찾을 수 없습니다.");
        }

        // 학생 상태 ACTIVE 로 변경
        studentRepository.updateStudentStatusDirect(studentId, "ACTIVE");

        // 한자 복구
        if (hanLog != null) {
            studentRepository.restoreHanState(
                    studentId,
                    hanLog.getBeforeHanState(),
                    hanLog.getBeforeHanClass(),
                    hanLog.getBeforeHanTeacher(),
                    hanLog.getBeforeEntryHanDate());
            studentRepository.updateWithdrawLogRestored(hanLog.getId(), userCode);
        }

        // 독서 복구
        if (bookLog != null) {
            studentRepository.restoreBookState(
                    studentId,
                    bookLog.getBeforeBookState(),
                    bookLog.getBeforeBookClass(),
                    bookLog.getBeforeBookTeacher(),
                    bookLog.getBeforeEntryBookDate());
            studentRepository.updateWithdrawLogRestored(bookLog.getId(), userCode);
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

        if (info == null)
            return;

        TeacherAssign assign = studentRepository.findTeacherAssign(studentId);
        String today = LocalDate.now().toString();

        boolean isHan = "1".equals(info.getClassType());
        boolean isBook = "2".equals(info.getClassType());

        User.builder().userCode(info.getTeacherCode()).build();
        ClassCode.builder().classKey(info.getClassKey()).build();

        boolean oldHan = assign.getHanState() != null && assign.getHanState();
        boolean oldBook = assign.getBookState() != null && assign.getBookState();

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

    public List<StudentTransferDTO> findInOutByStudentId(Integer studentId) {
        List<StudentTransferDTO> responseDTO = studentRepository.findInOutByStudentId(studentId);
        return responseDTO;
    }

    public List<StudentWebRespDTO.TransferHistoryDTO> getHistory(String centerCode, String yearMonth) {
        return studentRepository.selectTransferHistory(centerCode, yearMonth);
    }

    @Transactional
    public void reserveTransfer(StudentWebReqDTO.StudentTransferDTO reqDto, String userCode, String centerCode) {
        LocalDate moveDate = LocalDate.parse(reqDto.getMoveAt());

        if (moveDate.isBefore(LocalDate.now())) {
            throw new Exception400("과거 날짜로는 전입/전출할 수 없습니다.");
        }

        for (String studentId : reqDto.getStudents()) {

            StudentWebRespDTO.TeacherDTO teacher = studentRepository.findTeacherAssignByStudentId(studentId);

            if (reqDto.getSelectedHan() != null) {
                if (teacher.getAssignHanTeacher() == null) {
                    throw new Exception400("등록된 한자 수업이 없습니다.");
                }
                if (teacher.getAssignHanTeacher().equals(reqDto.getUserCode())) {
                    throw new Exception400("본인에게 전입/전출 할 수는 없습니다.");
                }

                // 중복 체크
                boolean hanExists = studentRepository.existsTransferSchedule(
                        studentId, teacher.getAssignHanTeacher());
                if (hanExists) {
                    throw new Exception400("이미 전입/전출이 등록되었습니다.");
                }

                studentRepository.insertTransferSchedule(
                        studentId,
                        "1",
                        teacher.getAssignHanTeacher(),
                        reqDto.getUserCode(),
                        reqDto.getMoveAt(),
                        reqDto.getTransferReason(),
                        userCode,
                        centerCode);
            }

            if (reqDto.getSelectedBook() != null) {
                if (teacher.getAssignBookTeacher() == null) {
                    throw new Exception400("등록된 독서 수업이 없습니다.");
                }
                if (teacher.getAssignBookTeacher().equals(reqDto.getUserCode())) {
                    throw new Exception400("본인에게 전입/전출 할 수는 없습니다.");
                }

                // 중복 체크
                boolean bookExists = studentRepository.existsTransferSchedule(
                        studentId, teacher.getAssignBookTeacher());
                if (bookExists) {
                    throw new Exception400("이미 전입/전출이 등록되었습니다.");
                }

                studentRepository.insertTransferSchedule(
                        studentId,
                        "2",
                        teacher.getAssignBookTeacher(),
                        reqDto.getUserCode(),
                        reqDto.getMoveAt(),
                        reqDto.getTransferReason(),
                        userCode,
                        centerCode);
            }
        }
    }

    public List<StudentWebRespDTO.PendingStudentRespDTO> findPendingStudent(String centerCode) {
        List<StudentWebRespDTO.PendingStudentRespDTO> student = studentRepository.findPendingStudent(centerCode);
        return student;
    }

    @Transactional
    public void applyTodayTransfers(LocalDate today) {

        String yy = String.valueOf(today.getYear());
        String mm = String.format("%02d", today.getMonthValue());

        List<StudentTransferSchedule> schedules = studentRepository.findTodaySchedules(today.toString());

        for (StudentTransferSchedule t : schedules) {

            // 1. 학생 전입/전출 반영
            studentRepository.updateTransfer(
                    t.getStudentId(),
                    t.getToUser(),
                    t.getClassType(),
                    yy,
                    mm);

            // 2. 시간표 / 결제 정리
            StudentWebRespDTO.TransferTimeTableInfoDTO dto = classRepository.findTimeTableKeyByStudentId(
                    t.getStudentId(),
                    t.getClassType(),
                    yy,
                    mm);

            if (dto != null) {
                classRepository.deleteByKeyAndStudentId(dto.getTimeTableKey(), t.getStudentId());
                paymentService.deleteDetail(dto.getTimeTableKey(), t.getStudentId());
            }

            // 3. 히스토리 저장
            StudentTransferHistory history = StudentTransferHistory.builder()
                    .student(Student.builder().studentId(t.getStudentId()).build())
                    .fromUser(User.builder().userCode(t.getFromUser()).build())
                    .toUser(User.builder().userCode(t.getToUser()).build())
                    .centerCode(Center.builder().centerCode(t.getCenterCode()).build())
                    .classType(t.getClassType())
                    .transferReason(t.getReason())
                    .updatedBy("SYSTEM")
                    .moveAt(today.toString())
                    .build();

            studentRepository.insertTransferHistory(history);

            // 4. 예약 완료 처리
            studentRepository.markAsApplied(t.getId());
        }
    }

    public StudentAppRespDTO.AppTokenRespDTO findAppTokenByAppId(String appId) {

        StudentAppRespDTO.AppTokenRespDTO respDTO;
        respDTO = studentRepository.findAppTokenByAppId(appId);

        if (respDTO == null) {
            String studentId = studentRepository.findAppIdByLog(appId);
            respDTO = studentRepository.findAppTokenByStudentId(studentId);
        }

        return respDTO;
    }

    public String checkinStudent(StudentAppReqDTO.StudentAttendanceDTO dto, Student student) {

        if (dto == null || student == null) {
            throw new RuntimeException("잘못된 요청 데이터입니다.");
        }

        List<StudentAttendance> existingAttendance = studentRepository.findByStudentAndDate(
                student.getStudentId(),
                dto.getYmd());

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

        // 체크인 시간
        LocalTime checkInTime = LocalTime.parse(dto.getHhmm());

        // ✅ 1. 현재 월 주차에서 날짜 매칭 확인
        List<ClassWeek> weekList = classRepository.findClassWeekByCenter(dto.getCenterCode(), year, month);
        String week = null;
        if (weekList != null && !weekList.isEmpty()) {
            week = findWeekByDate(today, weekList);
        }

        // ✅ 2. 현재 월에서 매칭 없으면 → 전월로 재조회
        if (week == null) {
            LocalDate prevMonth = today.minusMonths(1);
            String prevYear = String.valueOf(prevMonth.getYear());
            String prevMonthStr = String.format("%02d", prevMonth.getMonthValue());

            log.info("[체크인 주차계산] 현재 월 매칭 없음 → 전월({}-{}) 조회", prevYear, prevMonthStr);

            List<ClassWeek> prevWeekList = classRepository.findClassWeekByCenter(dto.getCenterCode(), prevYear,
                    prevMonthStr);
            if (prevWeekList != null && !prevWeekList.isEmpty()) {
                week = findWeekByDate(today, prevWeekList);
            }

            if (week != null) {
                year = prevYear;
                month = prevMonthStr;
                log.info("[체크인 주차계산] 전월({}-{})에서 찾음 → {}", year, month, week);
            }
        }

        if (week == null) {
            throw new RuntimeException("오늘 날짜는 어떤 주차에도 속하지 않습니다.");
        }

        // ✅ 3. 결정된 year, month 기준으로 수업 시간 조회
        List<ClassRespDTO.TimeRangeDTO> timeDTO = studentRepository.getStartClassTime(student.getStudentId(), year,
                month);
        if (timeDTO == null || timeDTO.isEmpty()) {
            throw new RuntimeException("수업 시간이 설정되지 않았습니다.");
        }

        List<ClassRespDTO.TimeRangeDTO> todayClasses = timeDTO.stream()
                .filter(t -> todayDayname.equals(t.getDayname()))
                .collect(Collectors.toList());

        // ✅ 4. 오늘 정규 수업 없으면 → 보강 확인
        if (todayClasses.isEmpty()) {
            List<StudentAppRespDTO.RemedialDTO> remedialList = studentRepository.findRemedialByStudentAndDate(
                    student.getStudentId(),
                    dto.getYmd());

            if (remedialList == null || remedialList.isEmpty()) {
                throw new RuntimeException("오늘은 수업이 없는 날입니다.");
            }
            boolean alreadyCheckedIn = remedialList.stream()
                    .anyMatch(r -> r.getInTime() != null && !r.getInTime().isEmpty());
            if (alreadyCheckedIn) {
                return "7777";
            }

            for (StudentAppRespDTO.RemedialDTO remedial : remedialList) {

                // 지각/정상 판단
                LocalTime start = LocalTime.parse(remedial.getSTime());
                String attendanceKey = checkInTime.isAfter(start) ? "late" : "present";

                // 1. erp_remedial UPDATE (보강 출결 시간)
                studentRepository.checkinRemedialAttendance(
                        remedial.getRemedialKey(),
                        dto.getHhmm(),
                        attendanceKey);

                // 2. 결석일 주차 정보 조회 (erp_class_week 기준)
                StudentAppRespDTO.AttendanceInfoDTO weekInfo = studentRepository
                        .findWeekInfoByDate(remedial.getAbsenceDate(), dto.getCenterCode());

                if (weekInfo == null) {
                    log.warn("결석일 주차 정보 없음 - studentId: {}, absenceDate: {}",
                            student.getStudentId(), remedial.getAbsenceDate());
                    continue;
                }

                // 3. erp_student_attendance is_remedial = 1 UPDATE
                studentRepository.updateIsRemedial(
                        student.getStudentId(),
                        remedial.getTimeTableKey(),
                        weekInfo.getWeek(),
                        weekInfo.getYy(),
                        weekInfo.getMm());

                log.info("보강 체크인 완료 - studentId: {}, remedialKey: {}, attendanceKey: {}",
                        student.getStudentId(), remedial.getRemedialKey(), attendanceKey);
            }

            return "0000";
        }

        // ✅ 5. 정규 수업 체크인
        for (ClassRespDTO.TimeRangeDTO targetClass : todayClasses) {
            LocalTime start = LocalTime.parse(targetClass.getStartTime());
            String attendanceKey = checkInTime.isAfter(start) ? "late" : "present";

            int updated = studentRepository.checkinStudentAttendance(
                    student.getStudentId(),
                    dto.getHhmm(),
                    targetClass.getEndTime(),
                    attendanceKey,
                    week,
                    year,
                    month,
                    targetClass.getTimeTableKey());

            log.info("정규 체크인 - studentId: {}, timeTableKey: {}, attendanceKey: {}, updated: {}",
                    student.getStudentId(), targetClass.getTimeTableKey(), attendanceKey, updated);
        }

        return "0000";
    }

    public String checkoutStudent(StudentAppReqDTO.StudentAttendanceDTO dto, Student student) {

        if (dto == null || student == null) {
            throw new RuntimeException("잘못된 요청 데이터입니다.");
        }

        List<StudentAttendance> attendanceList = studentRepository.findByStudentAndDate(
                student.getStudentId(),
                dto.getYmd());

        // 정규 수업 하원 처리
        if (attendanceList != null && !attendanceList.isEmpty()) {
            boolean alreadyCheckedOut = attendanceList.stream()
                    .anyMatch(attendance -> attendance.getOutTime() != null);

            if (alreadyCheckedOut) {
                return "6666";
            }

            int totalUpdated = studentRepository.checkoutStudentAttendance(
                    student.getStudentId(),
                    dto.getHhmm(),
                    dto.getYmd());

            if (totalUpdated > 0) {
                log.info("[체크아웃] 정규 수업 {}개 교시 하원 완료. studentId={}", totalUpdated, student.getStudentId());
                return "0000";
            }
        }

        // 정규 수업 기록 없으면 → 보강 하원 확인
        List<StudentAppRespDTO.RemedialDTO> remedialList = studentRepository.findRemedialByStudentAndDate(
                student.getStudentId(),
                dto.getYmd());

        if (remedialList == null || remedialList.isEmpty()) {
            log.info("[체크아웃] 등원 기록 없음. studentId={}, ymd={}", student.getStudentId(), dto.getYmd());
            return "8888";
        }

        // 보강 하원 처리
        for (StudentAppRespDTO.RemedialDTO remedial : remedialList) {
            // 이미 하원 처리된 경우
            if (remedial.getOutTime() != null && !remedial.getOutTime().isEmpty()) {
                return "6666";
            }

            studentRepository.checkoutRemedialAttendance(
                    remedial.getRemedialKey(),
                    dto.getHhmm());

            log.info("[체크아웃] 보강 하원 완료 - studentId: {}, remedialKey: {}",
                    student.getStudentId(), remedial.getRemedialKey());
        }

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

    // ========================================================================================

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
                        s.getWaitCount()))
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
    @Transactional
    public void updateAppToken(StudentAppReqDTO.AppTokenReqDTO dto) {

        Student student = studentRepository.findByStudentId(dto.getId());

        if (student == null) {
            return;
        }

        List<StudentWebRespDTO.SiblingInfoDTO> siblings = studentRepository
                .findSiblingByStudentId(student.getStudentId());
        log.info(siblings.toString());

        List<String> targetStudentIds = new ArrayList<>();
        targetStudentIds.add(student.getStudentId());

        log.info(targetStudentIds.toString());

        for (StudentWebRespDTO.SiblingInfoDTO sibling : siblings) {
            targetStudentIds.add(sibling.getStudentId());
            log.info(sibling.getStudentId());
        }

        for (String studentId : targetStudentIds) {

            int result = studentRepository.updateAppTokenByStudentId(studentId, dto.getToken());

            if (result > 0) {
                System.out.println("토큰 업데이트 완료: " + studentId);
            } else {
                System.out.println("이미 동일 토큰 또는 실패: " + studentId);
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
                month);

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
                month);

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
        List<StudentAppRespDTO.AttendanceListRespDTO> attendanceList = studentRepository.findAttendanceList(dto.getId(),
                yy, mm);
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

    public List<StudentAppRespDTO.AttendanceMainRespDTO> findAttendanceMain(
            StudentAppReqDTO.StudentAttendanceMainDTO dto) {
        return studentRepository.findAttendanceMain(dto.getId());
    }

    public List<StudentAppRespDTO.AppSiblingRespDTO> findSibling(StudentAppReqDTO.SiblingReqDTO reqDTO) {

        List<StudentAppRespDTO.AppSiblingRespDTO> siblings = studentRepository
                .findSiblingBySiblingKey(reqDTO.getSibling());

        // 각 형제의 ihak(book_code) 조회
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        for (StudentAppRespDTO.AppSiblingRespDTO sibling : siblings) {
            String ihak = studentRepository.getBookCodeByClassType(
                    sibling.getStuid(),
                    year,
                    month);
            sibling.setIhak(ihak);
        }

        return siblings;
    }

    @Transactional
    public void updateAttendance(StudentWebReqDTO.StudentAttendanceUpdateDTO dto, String userCode) {

        String userId = getCurrentUserId();
        attendanceRepository.setSessionContext("user_id", userId);

        // 1. 이전 출결 상태 조회
        String prevKey = studentRepository.findAttendanceKey(
                dto.getStudentId(),
                dto.getTimeTableKey(),
                dto.getWeek());

        // 2. 출석, 지각으로 변경시 등원 시간 없으면 수업 시작시간을 기본값으로 지정
        if (("present".equals(dto.getAttendanceKey()) || "late".equals(dto.getAttendanceKey()))
                && dto.getInTime() == null) {
            String classStartTime = attendanceRepository.findClassStartTime(dto.getTimeTableKey());
            if (classStartTime != null) {
                dto.setInTime(classStartTime);
            }
        }

        // 3. 출결 업데이트
        int updated = studentRepository.updateAttendance(dto);
        if (updated == 0) {
            throw new RuntimeException("출석 정보를 찾을 수 없거나 업데이트에 실패했습니다.");
        }

        String newKey = dto.getAttendanceKey();

        // 4. 결석 → 보강 생성
        if (!"absent".equals(prevKey) && "absent".equals(newKey)) {
            Map<String, String> ttYyMm = attendanceRepository.findYyMmByTimeTableKey(dto.getTimeTableKey());
            attendanceRepository.insertRemedialForStudent(
                    dto.getStudentId(),
                    dto.getTimeTableKey(),
                    dto.getAbsenceDate(),
                    dto.getWeek(),
                    ttYyMm.get("yy"),
                    ttYyMm.get("mm"),
                    userCode);

        }

        // 5. 결석 해제 → 보강 삭제
        if ("absent".equals(prevKey) && !"absent".equals(newKey)) {
            attendanceRepository.deleteRemedialForStudent(
                    dto.getStudentId(),
                    dto.getTimeTableKey(),
                    dto.getWeek());
        }
    }

    private String getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    public void cancelJoin(String studentId) {
        if (studentRepository.countStudent(studentId) == 0) {
            throw new IllegalArgumentException("존재하지 않는 학생입니다.");
        }
        try {
            studentRepository.cancelJoinStudent(studentId);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null) {
                String msg = cause.getMessage();
                log.info(msg);
                if (msg != null) {
                    if (msg.contains("결제 이력이 있습니다. 결제를 먼저 취소해주세요.")) {
                        throw new IllegalArgumentException("결제 내역이 있습니다. 결제를 먼저 취소해주세요.");
                    }
                    if (msg.contains("수업 이력이 존재하는 학생은 입회 취소가 불가합니다.")) {
                        throw new IllegalArgumentException("수업 이력이 존재하는 학생은 입회 취소가 불가합니다.");
                    }
                }
                cause = cause.getCause();
            }
            throw new RuntimeException("입회 취소 처리 중 오류가 발생했습니다.", e);
        }
    }

    public StudentWebRespDTO.WithdrawCountDTO findWithdrawCounts(StudentWebReqDTO.WithdrawReqDTO req) {
        return studentRepository.selectWithdrawCounts(req);
    }

    public List<StudentWebRespDTO.WithdrawItemDTO> findJoinList(StudentWebReqDTO.WithdrawReqDTO req) {
        return studentRepository.selectJoinList(req);
    }

    public List<StudentWebRespDTO.WithdrawItemDTO> findWithdrawList(StudentWebReqDTO.WithdrawReqDTO req) {
        List<StudentWebRespDTO.WithdrawItemDTO> rawList = studentRepository.selectWithdrawList(req);

        Map<String, StudentWebRespDTO.WithdrawItemDTO> map = new LinkedHashMap<>();

        for (StudentWebRespDTO.WithdrawItemDTO item : rawList) {
            if (map.containsKey(item.getStudentId())) {
                StudentWebRespDTO.WithdrawItemDTO existing = map.get(item.getStudentId());

                if (item.getClassName() != null && !item.getClassName().equals(existing.getClassName())) {
                    existing.setClassName(existing.getClassName() + ", " + item.getClassName());
                }
                if (item.getTeacherName() != null && !item.getTeacherName().equals(existing.getTeacherName())) {
                    existing.setTeacherName(existing.getTeacherName() + ", " + item.getTeacherName());
                }
            } else {
                map.put(item.getStudentId(), item);
            }
        }

        return new ArrayList<>(map.values());
    }

    public List<StudentWebRespDTO.WithdrawItemDTO> findTransferInList(StudentWebReqDTO.WithdrawReqDTO req) {
        return studentRepository.selectTransferInList(req);
    }

    public List<StudentWebRespDTO.WithdrawItemDTO> findTransferOutList(StudentWebReqDTO.WithdrawReqDTO req) {
        return studentRepository.selectTransferOutList(req);
    }

    public List<StudentWebRespDTO.WithdrawItemDTO> findGraduateList(StudentWebReqDTO.WithdrawReqDTO req) {
        return studentRepository.selectGraduateList(req);
    }

    public List<StudentWebRespDTO.SearchRespDTO> searchByName(String studentName) {
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("학생 이름을 입력해 주세요.");
        }

        List<StudentWebRespDTO.SearchRespDTO> result = studentRepository.searchByStudentName(studentName);
        log.info("[AppId] 학생 검색 - name={}, 결과 {}건", studentName, result.size());
        return result;
    }

    @Transactional
    public void updateAppIdLog(StudentWebReqDTO.UpdateAppIdLogDTO req) {
        req.setManual(true);
        studentRepository.updateAppIdLog(req);
    }

    @Transactional
    public void cancelPending(Integer id, String userCode) {
        int count = studentRepository.countByPendingId(id);
        if (count == 0) {
            throw new IllegalArgumentException("존재하지 않는 학생입니다. id=" + id);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("userCode", userCode);

        studentRepository.insertToPendingDel(id, userCode);

        studentRepository.deleteByPendingId(id);
    }

    public void updateStudentBillingPhone(String studentId, String phone) {
        // 유효성 검사
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("학생 ID가 없습니다.");
        }
        if (phone == null || phone.replaceAll("[^0-9]", "").length() < 10) {
            throw new IllegalArgumentException("올바른 전화번호가 아닙니다.");
        }

        studentRepository.updateStudentBillingPhone(studentId, phone);
    }

    public List<StudentWebRespDTO.SiblingSearchRespDTO> searchSibling(String currentStudentId, String searchKey,
                                                                      String searchValue) {

        // 현재 학생의 center_code 조회 (같은 센터 내에서만 검색)
        String centerCode = studentRepository.getCenterCode(currentStudentId);

        if ("phone".equals(searchKey)) {
            searchValue = searchValue.replaceAll("-", "");
        }

        return studentRepository.searchFamily(searchKey, searchValue, centerCode);
    }

    public void linkFamily(StudentWebReqDTO.FamilyLinkRequest req) {

        String studentId = req.getStudentId();
        String siblingId = req.getSiblingId();

        // 이미 같은 그룹인지 확인
        if (studentRepository.existsFamilyLink(studentId, siblingId)) {
            throw new Exception400("이미 형제로 연결된 학생입니다.");
        }

        String centerCode = studentRepository.getCenterCode(studentId);

        String studentGroupKey = studentRepository.getSiblingGroupKey(studentId);
        String siblingGroupKey = studentRepository.getSiblingGroupKey(siblingId);

        if (studentGroupKey == null && siblingGroupKey == null) {
            String newGroupKey = studentRepository.generateNewGroupKey();
            studentRepository.insertSiblingMember(newGroupKey, studentId, centerCode);
            studentRepository.insertSiblingMember(newGroupKey, siblingId, centerCode);

        } else if (studentGroupKey != null && siblingGroupKey == null) {
            studentRepository.insertSiblingMember(studentGroupKey, siblingId, centerCode);

        } else if (studentGroupKey == null) {
            studentRepository.insertSiblingMember(siblingGroupKey, studentId, centerCode);

        } else {
            studentRepository.mergeGroups(studentGroupKey, siblingGroupKey);
        }
    }

    public List<StudentWebRespDTO.SiblingSearchRespDTO> getFamilyList(String studentId) {
        return studentRepository.getSiblingList(studentId);
    }

    // 형제 연결 삭제
    public void unlinkFamily(StudentWebReqDTO.FamilyLinkRequest req) {
        String studentId = req.getStudentId();
        String siblingId = req.getSiblingId();

        // 그룹 내 인원 수 확인
        String groupKey = studentRepository.getSiblingGroupKey(studentId);
        if (groupKey == null) {
            throw new Exception400("형제 관계가 존재하지 않습니다.");
        }

        int groupCount = studentRepository.countGroupMembers(groupKey);

        if (groupCount <= 2) {
            // 2명 이하면 그룹 전체 삭제
            studentRepository.deleteGroup(groupKey);
        } else {
            // 3명 이상이면 해당 학생만 삭제
            studentRepository.deleteSiblingMember(groupKey, siblingId);
        }
    }

    @Transactional
    public String registerQr(StudentWebReqDTO.QrRegisterDTO request, String centerCode) {
        String qrNumber = request.getQrNumber();

        // 1. 실제 존재하는 QR 번호인지
        if (studentRepository.countByQrNumber(qrNumber, centerCode) == 0) {
            return "사용할 수 없는 카드 번호입니다.";
        }

        // 2. 현재 사용 중인 번호인지
        int inUse = studentRepository.countInUse(qrNumber, centerCode);
        if (inUse > 0) {
            return "사용 중인 카드 번호입니다.";
        }

        // 3. 등록
        studentRepository.registerQrNumber(request.getStudentId(), qrNumber, centerCode);
        studentRepository.updateQrNumber(request.getStudentId(), qrNumber);
        return null;
    }

    @Transactional
    public String updateQr(String studentId, String qrNumber, String centerCode) {
        if (studentRepository.countByQrNumber(qrNumber, centerCode) == 0)
            return "등록되지 않은 QR 카드번호입니다.";
        studentRepository.unlinkQrByStudentId(studentId, centerCode);
        studentRepository.registerQrNumber(studentId, qrNumber, centerCode);
        studentRepository.updateQrNumber(studentId, qrNumber);
        return null;
    }
}
