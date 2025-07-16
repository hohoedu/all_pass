package com.hohoedu.all_pass.class_instance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.AddStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance.code.ClassCode;
import com.hohoedu.all_pass.class_instance.code.UnitCode;
import com.hohoedu.all_pass.class_instance.repository.ClassCodeJpaRepository;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import com.hohoedu.all_pass.class_instance.repository.UnitCodeJpaRepository;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.repository.GradeJpaRepository;
import com.hohoedu.all_pass.user.model.User;
import com.hohoedu.all_pass.user.repository.UserJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UnitCodeJpaRepository unitCodeJpaRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final UserJpaRepository userJpaRepository;
    // private final TimeTableAssignJpaRepository assignJpaRepository;

    public List<ClassCode> findClassCode() {
        List<ClassCode> classCodes = classCodeJpaRepository.findAll();
        return classCodes;
    }

    public List<UnitCode> findUnitCode() {
        List<UnitCode> unitCodes = unitCodeJpaRepository.findAll();
        return unitCodes;
    }

    public List<GradeCode> findGrade() {
        List<GradeCode> grades = gradeJpaRepository.findAll();
        return grades;
    }

    public String registerClass(ClassReqDTO.ClassRegisterDTO classReqDTO) {
        TimeTableDTO timeTable = classRepository.existsByYearAndMonthAndPeriodNo(
                classReqDTO.getPeriodNo(), classReqDTO.getYy(), classReqDTO.getMm(), classReqDTO.getDayname());

        boolean isEmpty = timeTable == null;

        if (isEmpty) {
            // 인서트
            classRepository.registerClass(classReqDTO);
            return "success-register";
        } else {

            // 업데이트
            System.out.println("업데이트");
            classRepository.updateClass(classReqDTO, timeTable.getTimeTableNo());
            return "success-update";

        }
    }

    public List<TimeTableDTO> findTimeTableWithStudents(String year, String month) {

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(2, year, month);

        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = classRepository
                    .findStudentsByTimeTableNo(tt.getTimeTableNo());

            tt.setStudents(students);
        }
        return tables;
    }

    public List<TimeTableDTO> findTableViewWithStudents(String year, String month, Integer userNo) {

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userNo, year, month);

        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = classRepository
                    .findStudentsByTimeTableNo(tt.getTimeTableNo());

            while (students.size() < 8) {
                ClassRespDTO.TimeTableDTO.StudentDTO empty = new ClassRespDTO.TimeTableDTO.StudentDTO();
                empty.setStudentName("\u00A0");
                students.add(empty);
            }

            tt.setStudents(students);
        }
        return tables;
    }

    public boolean addStudent(AddStudentDTO dto) {
        int count = classRepository.countByTimeTableNo(dto.getTimeTableNo());
        if (count >= 8) {
            return false;
        }
        classRepository.addStudent(dto);
        return true;
    }

    public void deleteStudent(Integer assignNo) {
        // assignJpaRepository.findById(assignNo).orElseThrow(() -> new
        // Exception404("학생을 찾을 수 없습니다."));

        classRepository.deleteByAssignNo(assignNo);
    }

    public List<User> findUsers() {
        List<User> users = userJpaRepository.findAll();
        System.out.println(users);
        return users;
    }
}
