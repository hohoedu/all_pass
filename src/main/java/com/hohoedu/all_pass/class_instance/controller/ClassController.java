package com.hohoedu.all_pass.class_instance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO;
import com.hohoedu.all_pass.class_instance.service.ClassService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping("/register")
    public ResponseEntity<?> registerClass(@RequestBody List<ClassReqDTO.ClassRegisterDTO> reqDTO) {
        for (ClassReqDTO.ClassRegisterDTO req : reqDTO) {
            classService.registerClass(req);

        }

       return ResponseEntity.ok(Map.of("response", "success"));
    }

    @PostMapping("/add_student")
    @ResponseBody
    public ResponseEntity<?> timeTableAssginStudent(@RequestBody ClassReqDTO.AddStudentList reqDTO) {

        try {
            boolean isSuccess = reqDTO.getAssignments().stream()
                    .allMatch(dto -> classService.addStudent(dto));
            if (isSuccess) {
                return ResponseEntity
                        .ok(ApiUtils.success(true));
            } else {
                return ResponseEntity
                        .ok(ApiUtils.error("최대 8명까지 등록 가능합니다.", HttpStatus.OK));
            }
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity
                    .ok(ApiUtils.error("이미 추가된 학생입니다.", HttpStatus.OK));
        }
    }

    @PostMapping("/delete_student")
    public ResponseEntity<?> timeTableDeleteStudent(@RequestParam("timeTableAssignNo") Integer assignNo) {
        classService.deleteStudent(assignNo);
        return ResponseEntity.ok(ApiUtils.success(true));
    }

}
