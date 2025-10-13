package com.hohoedu.all_pass.app;

import com.hohoedu.all_pass._core.utils.AppApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
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

        List<ClassAppRespDTO.ClassInfoRespDTO> respDTOs = classService.getClassInfo(reqDTO.getId(), reqDTO.getYyyy(), reqDTO.getMm());

        return ResponseEntity.ok(AppApiUtils.successList(respDTOs));
    }

    @PostMapping("/attendance_main")
    public ResponseEntity<?> AppAttendanceMain() {

        System.out.println("hello!! attendance main");

        return ResponseEntity.ok(AppApiUtils.successOne(null));
    }

    @PostMapping("/course_book_main")
    public ResponseEntity<?> AppCourseBookMain(@RequestBody ClassAppReqDTO.BookListReqDTO reqDTO) {

        System.out.println("hello!! book_main");

        return ResponseEntity.ok(AppApiUtils.successOne(null));
    }

    @PostMapping("/book_list")
    public ResponseEntity<?> AppBookList(@RequestBody ClassAppReqDTO.ClinicBookReqDTO reqDTO) {
        System.out.println("hello!! book list");
        return ResponseEntity.ok(AppApiUtils.successList(null));
    }

    @PostMapping("/before_class_notice")
    public ResponseEntity<?> AppBeforeNotice(@RequestBody ClassAppReqDTO.BeforeClassReqDTO reqDTO) {

        List<ClassAppRespDTO.BeforeClassRespDTO> respDTOs = classService.getBeforeClass(reqDTO.getId(), reqDTO.getCount());

        return ResponseEntity.ok(AppApiUtils.successList(respDTOs));
    }

    @PostMapping("/learning_contents")
    public ResponseEntity<?> AppLearningContents(@RequestBody ClassAppReqDTO.LearningContentsReqDTO reqDTO) {

        return ResponseEntity.ok(AppApiUtils.successList(null));
    }

    @PostMapping("/monthly_notice")
    public ResponseEntity<?> AppMonthlyNotice(@RequestBody ClassAppReqDTO.BeforeClassReqDTO reqDTO) {

        List<ClassAppRespDTO.BeforeClassRespDTO> respDTOs = classService.getBeforeClass(reqDTO.getId(), reqDTO.getCount());

        return ResponseEntity.ok(AppApiUtils.successList(respDTOs));
    }

    @PostMapping("/infant_notice")
    public ResponseEntity<?> AppInfantNotice(@RequestBody ClassAppReqDTO.BeforeClassReqDTO reqDTO) {


        return ResponseEntity.ok(AppApiUtils.successList(null));
    }
}