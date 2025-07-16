package com.hohoedu.all_pass.student.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass.parent.code.RelationCode;
import com.hohoedu.all_pass.parent.service.ParentService;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.model.Student;
import com.hohoedu.all_pass.student.repository.GradeJpaRepository;
import com.hohoedu.all_pass.student.repository.StudentJpaRepository;
import com.hohoedu.all_pass.student.repository.StudentRepository;
import com.hohoedu.all_pass.user.model.User;
import com.hohoedu.all_pass.user.repository.UserJpaRepository;
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
        private final UserJpaRepository userJpaRepository;

        private final ParentService parentService;

        public List<Student> findAllByJpa() {
                List<Student> student = studentJpaRepository.findAll();
                return student;
        }

        public List<Student> findAllByMyBatis() {
                List<Student> student = studentRepository.findAll();

                return student;
        }

        public StudentRespDTO.StudentDTO findStudentByStudentId(Integer studentId) {

                System.out.println("여긴 호출 돼");
                System.out.println(studentId);
                StudentRespDTO.StudentDTO student = studentRepository.findStudentByStudentId(studentId);

                return student;
        }

        public void studentInsert(StudentReqDTO.StudentJoinDTO studentDTO, StudentReqDTO.ParentJoinDTO parentDTO) {
                studentDTO.setStudentId("hello");

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

        public List<StudentInOutDTO> findAllInOut() {

                List<StudentInOutDTO> students = studentRepository.findAllInOut();

                return students;
        }

        public List<User> findTeacher() {
                List<User> users = userJpaRepository.findAll();
                System.out.println(users);
                return users;
        }

        public void statusInsert(StudentReqDTO.StatusHistoryDTO historyDTO) {
                studentRepository.statusInsert(historyDTO);
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

        public void transferStudent(StudentReqDTO.StudentTransferDTO studentTransferDTO) {
                // for (String studentNo : studentTransferDTO.getStudentNo()) {

                studentRepository.transferOne();

                // }
        }

}
