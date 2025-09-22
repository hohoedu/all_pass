package com.hohoedu.all_pass._core;

import com.hohoedu.all_pass._core.utils.AppApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student._dto.app.StudentAppReqDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppReqDTO;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppController {

    private final StudentService studentService;
    private final ClassService classService;

    @PostMapping("/login")
    public ResponseEntity<?> AppLogin(@RequestBody StudentAppReqDTO.LoginReqDTO reqDTO) {

        StudentAppRespDTO.AppLoginRespDTO respDTO = studentService.checkAppIdAndPassword(reqDTO.getId(), reqDTO.getSha_pwd());

        return ResponseEntity.ok(AppApiUtils.successOne(respDTO));
    }

    @PostMapping("/token")
    public ResponseEntity<?> AppTokenSave(@RequestBody StudentAppReqDTO.AppTokenReqDTO reqDTO) {

        studentService.updateAppToken(reqDTO);

        return ResponseEntity.ok(AppApiUtils.successOne(null));
    }

    @PostMapping("/class_info")
    public ResponseEntity<?> AppClassInfo(@RequestBody ClassAppReqDTO.ClassInfoReqDTO reqDTO) {
        System.out.println("======================================");
        System.out.println("==      id =" + reqDTO.getId());
        System.out.println("======================================");
        System.out.println("==      yyyy" + reqDTO.getYyyy());
        System.out.println("======================================");
        System.out.println("==      mm =" + reqDTO.getMm());
        System.out.println("======================================");

        List<ClassAppRespDTO.ClassInfoRespDTO> respDTOs = classService.getClassInfo(reqDTO.getId(), reqDTO.getYyyy(), reqDTO.getMm());
        System.out.println("==============================");
        System.out.println("응답 직전");
        System.out.println("==============================");
        return ResponseEntity.ok(AppApiUtils.successList(respDTOs));
    }

    @PostMapping("/attendance_main")
    public ResponseEntity<?> AppAttendanceMain() {
        System.out.println("hello!! attendance main");
        return ResponseEntity.ok(AppApiUtils.successOne(null));
    }

    @PostMapping("/book_list")
    public ResponseEntity<?> AppBookList() {
        System.out.println("hello!! book list");
        return ResponseEntity.ok(AppApiUtils.successList(null));
    }
}