package com.hohoedu.all_pass.app;

import com.hohoedu.all_pass._core.utils.AppApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import com.hohoedu.all_pass.notice.NoticeService;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppReqDTO;
import com.hohoedu.all_pass.notice._dto.app.NoticeAppRespDTO;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppReqDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student._dto.app.StudentAppReqDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppReqDTO;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppController {

    private final StudentService studentService;
    private final ClassService classService;
    private final PaymentService paymentService;
    private final NoticeService noticeService;
    private final AppService appService;

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

    // 메인화면
    @PostMapping("/course_book_main")
    public ResponseEntity<?> AppCourseBookMain(@RequestBody ClassAppReqDTO.BookListMainReqDTO reqDTO) {

        ClassAppRespDTO.BookListMainRespDTO response = appService.getBookMainInfo(reqDTO);

        return ResponseEntity.ok(AppApiUtils.successOne(response));
    }

    // 도서 상세 화면
    @PostMapping("/course_book")
    public ResponseEntity<?> AppCourseBook(@RequestBody ClassAppReqDTO.BooklistReqDTO reqDTO) {

        ClassAppRespDTO.BookListRespDTO response = appService.getBookInfo(reqDTO);

        return ResponseEntity.ok(AppApiUtils.successOne(response));
    }

    @PostMapping("/before_class_notice")
    public ResponseEntity<?> AppBeforeNotice(@RequestBody ClassAppReqDTO.BeforeClassReqDTO reqDTO) {

        List<ClassAppRespDTO.BeforeClassRespDTO> respDTOs = classService.getBeforeClass(reqDTO.getId(), reqDTO.getCount());

        return ResponseEntity.ok(AppApiUtils.successList(respDTOs));
    }

    @PostMapping("/learning_contents")
    public ResponseEntity<?> AppLearningContents(@RequestBody ClassAppReqDTO.LearningContentsReqDTO reqDTO) {
        List<ClassAppRespDTO.AfterClassRespDTO> respDTOs = classService.getAfterClass(reqDTO.getId(), reqDTO.getCount());
        return ResponseEntity.ok(AppApiUtils.successList(respDTOs));
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

    @PostMapping("/payment_details")
    public ResponseEntity<?> AppPaymentDetails(@RequestBody PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
        System.out.println(reqDTO.getStudentId());
        List<PaymentAppRespDTO.PaymentDetailRespDTO> dataList = paymentService.findPaymentDetailsBytudentId(reqDTO);


        return ResponseEntity.ok(AppApiUtils.successList(dataList));
    }

    @PostMapping("/notice_list")
    public ResponseEntity<?> AppNoticeList(@RequestBody NoticeAppReqDTO.NoticeAppListReqDTO reqDTO) {
        List<NoticeAppRespDTO.NoticeListRespDTO> noticeList = noticeService.findAppNoticeList(reqDTO);
        return ResponseEntity.ok(AppApiUtils.successList(noticeList));
    }

    @PostMapping("/notice_view")
    public ResponseEntity<?> AppNoticeView(@RequestBody Map<String, Integer> idx) {
        NoticeAppRespDTO.NoticeDetailRespDTO noticeDetail = noticeService.findAppNoticeDetail(idx.get("idx"));
        return ResponseEntity.ok(AppApiUtils.successOne(noticeDetail));
    }

    @PostMapping("/book_list")
    public ResponseEntity<?> AppBookListView(@RequestBody ClassAppReqDTO.ClinicBookListReqDTO dto) {
        List<ClassAppRespDTO.ClinicListRespDTO> clinicList = classService.findClinicList(dto);
        return ResponseEntity.ok(AppApiUtils.successClinicList(clinicList));
    }

    @PostMapping("/book_result")
    public ResponseEntity<?> AppBookResult(@RequestBody ClassAppReqDTO.ClinicBookResultReqDTO dto) {
        List<ClassAppRespDTO.ClinicResultRespDTO> clinicResult = classService.findClinicResult(dto);
        return ResponseEntity.ok(AppApiUtils.successClinicList(clinicResult));
    }

    @PostMapping("/book_total_list")
    public ResponseEntity<?> AppBookTotalList(@RequestBody ClassAppReqDTO.ClinicBookTotalListReqDTO dto) {
List<ClassAppRespDTO.ClinicTotalRespDTO> clinicTotal = classService.findClinicTotal(dto);
        return ResponseEntity.ok(AppApiUtils.successClinicList(clinicTotal));
    }
}
