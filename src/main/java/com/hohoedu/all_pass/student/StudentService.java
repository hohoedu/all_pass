package com.hohoedu.all_pass.student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.repository.ClassCodeJpaRepository;
import com.hohoedu.all_pass.parent.ParentService;
import com.hohoedu.all_pass.parent.code.RelationCode;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.student.model.StudentTransferHistory;
import com.hohoedu.all_pass.student.repository.GradeJpaRepository;
import com.hohoedu.all_pass.student.repository.StudentJpaRepository;
import com.hohoedu.all_pass.student.repository.StudentRepository;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.repository.UserRepository;
import com.hohoedu.all_pass.parent.repository.RelationJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final RelationJpaRepository relationJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final UserRepository userRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;

    private final ParentService parentService;

    public List<Student> findAllByJpa() {
        List<Student> student = studentJpaRepository.findAll();
        return student;
    }

    public List<Student> findAll(String centerNo) {
        List<Student> student = studentRepository.findAll(centerNo);

        return student;
    }

    public List<MainStudentDTO> getStudentsByClassCode(String timeTableCode) {

        List<MainStudentDTO> rows = studentRepository.selectStudentByClassCode(timeTableCode);
        return rows;
    }

    public List<MainStudentDTO> getStudentsByUserCode(String userCode) {
        System.out.println(userCode);
        List<MainStudentDTO> rows = studentRepository.selectStudentByUserCode(userCode);
        return rows;
    }

    public StudentRespDTO.StudentDTO findStudentByStudentId(Integer studentId) {

        StudentRespDTO.StudentDTO student = studentRepository.findStudentByStudentId(studentId);

        return student;
    }

    public void studentInsert(StudentReqDTO.StudentJoinDTO studentDTO, StudentReqDTO.ParentJoinDTO parentDTO) {

        studentDTO.setStudentId("DAE001250730A1B2");
        studentRepository.insert(studentDTO);
        parentDTO.setStudentNo(studentDTO.getStudentNo());
        parentService.parentInsert(parentDTO);
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

    public List<StudentInOutDTO> findAllInOut() {
        List<StudentInOutDTO> students = studentRepository.selectTransferStudents();
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
        System.out.println("========================");
        System.out.println(responseDTO);
        System.out.println("========================");
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
