package com.hohoedu.all_pass.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.repository.CenterRepository;
import com.hohoedu.all_pass.family.FamilyService;
import com.hohoedu.all_pass.student.model.StudentSnapshot;
import com.hohoedu.all_pass.student.model.StudentSnapshotId;
import com.hohoedu.all_pass.student.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.repository.ClassCodeJpaRepository;
import com.hohoedu.all_pass.parent.ParentService;
import com.hohoedu.all_pass.family.model.RelationCode;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.student.model.StudentTransferHistory;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.repository.UserRepository;
import com.hohoedu.all_pass.family.repository.RelationJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final RelationJpaRepository relationJpaRepository;
    private final UserRepository userRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;
    private final SnapshotRepository snapshotRepository;
    private final SnapshotJpaRepository snapshotJpaRepository;
    private final CenterRepository centerRepository;

    private final FamilyService familyService;

    public List<Student> findStudentByCenterCode(String centerCode) {

        List<Student> student = studentRepository.findstudentByCenterCode(centerCode);

        return student;
    }

    public List<MainStudentDTO> getStudentsByKey(String timeTableKey, String userCode) {
        List<MainStudentDTO> rows = studentRepository.selectStudentByKey(timeTableKey, userCode);
        return rows;
    }

    public List<MainStudentDTO> getStudentsByUserCode(String userCode, String centerCode) {
        System.out.println(userCode);
        List<MainStudentDTO> rows = studentRepository.selectStudentByUserCode(userCode, centerCode);
        return rows;
    }

    public StudentRespDTO.StudentDTO findStudentByStudentId(String studentId) {

        StudentRespDTO.StudentDTO student = studentRepository.findStudentByStudentId(studentId);

        return student;
    }

    public void studentInsert(StudentReqDTO.StudentJoinDTO studentDTO, StudentReqDTO.ParentJoinDTO parentDTO) {

        studentDTO.setStudentId("DAE001250730A1B2");
        studentRepository.insert(studentDTO);
        parentDTO.setStudentNo(studentDTO.getStudentNo());
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
        List<StudentInOutDTO> students = studentRepository.selectTransferStudents(centerCode);
        return students;
    }

    public List<User> findTeacher(String centerCode) {
        List<User> users = userRepository.findUserByCenterCode(centerCode);
        System.out.println(users);
        return users;
    }

    public void statusInsert(StudentReqDTO.StatusHistoryDTO historyDTO) {
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
    }

    public List<GradeCode> getGrade() {
        List<GradeCode> gradeCodes = gradeJpaRepository.findAll();
        return gradeCodes;
    }

    public List<StudentTransferDTO> findInOutByStudentId(Integer studentId) {
        List<StudentTransferDTO> responseDTO = studentRepository.findInOutByStudentId(studentId);
        return responseDTO;

    }

    public void transferStudent(StudentReqDTO.StudentTransferDTO reqDto) {
        try {
            if (reqDto.getInoutHan() != null) {
                studentRepository.transfer(
                        reqDto.getUserCode(),
                        reqDto.getStudentNoList().get(0),
                        reqDto.getInoutHan());
            }
            if (reqDto.getInoutRead() != null) {
                studentRepository.transfer(
                        reqDto.getUserCode(),
                        reqDto.getStudentNoList().get(0),
                        reqDto.getInoutRead());
            }
        } catch (Exception e) {
        }

    }

    public void insertTransferHistory(StudentReqDTO.StudentTransferDTO dto) {
        try {
            for (Integer id : dto.getStudentNoList()) {
                StudentTransferHistory history = StudentTransferHistory.builder()
                        .student(Student.builder().studentId("hello").build())
                        .fromUser(User.builder().userCode("DAE001cos").build())
                        .toUser(User.builder().userCode(dto.getUserCode()).build())
                        .classType(dto.getInoutHan())
                        .transferReason(dto.getTransferReason())
                        .moveAt(dto.getMoveAt())
                        .build();
                studentRepository.insertTransferHistory(history);
            }
        } catch (Exception e) {

        }

    }

    // ================================================================================================================
    private LocalDateTime endOfMonthDateTime(String ym) {
        int y = Integer.parseInt(ym.substring(0,4));
        int m = Integer.parseInt(ym.substring(4,6));
        LocalDate end = YearMonth.of(y, m).atEndOfMonth();
        return end.atTime(23, 59, 59);
    }

    /** (A) 특정 월 실시간 집계 후 스냅샷 저장/갱신 */
    @Transactional
    public void upsertSnapshot(String centerCode, String ym) {
        LocalDateTime monthEnd = endOfMonthDateTime(ym);
        Map<String, Object> r = snapshotRepository.aggregateAtMonthEnd(centerCode, monthEnd);

        int total     = toInt(r.get("total_count"));
        int active    = toInt(r.get("active_count"));
        int rest      = toInt(r.get("rest_count"));
        int withdrawn = toInt(r.get("withdrawn_count"));
        int wait      = toInt(r.get("wait_count"));

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

    /** (B) 구간 스냅샷 조회 (포함) */
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

    public Student findByAppId(String appId) {
        Student student = studentRepository.findByAppId(appId);
        return student;
    }

}
