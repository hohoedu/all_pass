package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass._core.utils.FileUploadService;
import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.payment.PaymentReconcileService;
import com.hohoedu.all_pass.payment._dto.web.ReconcileDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FileUploadService fileUploadService;
    private final PaymentReconcileService paymentReconcileService;

    @PostMapping("/save/person")
    public ResponseEntity<?> createPersonYear(@RequestBody AdminReqDTO.PersonSettingDTO reqDTO) {
        int response = adminService.insertPersonYear(reqDTO);
        if (response > 0) {
            return ResponseEntity.ok(ApiUtils.success("저장되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(ApiUtils.error("저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/load/person")
    public ResponseEntity<AdminReqDTO.PersonSettingDTO> loadPersonSetting(@RequestBody AdminReqDTO.PersonFindDTO reqDTO) {

        AdminReqDTO.PersonSettingDTO result = adminService.loadPersonYear(reqDTO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/load/keycode")
    public ResponseEntity<?> loadKeycodeList(@RequestBody AdminReqDTO.KeycodeFindDTO reqDTO) {
        List<AdminRespDTO.KeycodeDTO> result = adminService.findKeycodeList(reqDTO);
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/save/keycode")
    public ResponseEntity<?> saveKeycode(@RequestBody AdminReqDTO.KeycodeSaveDTO reqDTO) {
        try {
            int saved = adminService.saveKeycodeList(reqDTO);
            return ResponseEntity.ok(ApiUtils.success(saved + "건 저장되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiUtils.error("저장 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/save/book")
    public Object saveBooks(@RequestBody AdminReqDTO.BookSuggestSaveReqDTO reqDTO) {
        try {
            log.info(reqDTO.toString());
            adminService.saveBookSuggest(reqDTO);

            return ApiUtils.success("저장되었습니다.");
        } catch (Exception e) {
            return ApiUtils.error("저장 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/book/suggest")
    @ResponseBody
    public ResponseEntity<?> getBookSuggest(@RequestBody AdminReqDTO.BookSuggestSearchReqDTO req) {
        List<AdminRespDTO.BookSuggestViewDTO> result = adminService.findBookSuggestByMonth(req.getClassKey(), req.getYy(), req.getMm());

        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/upload/book-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadedPath = fileUploadService.uploadCourseBook(file);
            return ResponseEntity.ok(ApiUtils.success(uploadedPath));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("UPLOAD_FAIL");
        }
    }

    @PostMapping("/delete/book-image")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        try {
            fileUploadService.deleteFromFTP(url);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/center/status/data")
    public ResponseEntity<?> getCenterStatus(
            @RequestParam(value = "year") String year,
            @RequestParam(value = "month") String month) {
        return ResponseEntity.ok(ApiUtils.success(adminService.findCenterStatus(year, month)));
    }

    // 입탈퇴 조회 (센터·연도)
    @GetMapping("/center/member-change/data")
    public ResponseEntity<?> getMemberChange(
            @RequestParam(value = "centerCode") String centerCode,
            @RequestParam(value = "year") String year,
            @RequestParam(value = "userCodes", required = false) Set<String> userCodes) {
        return ResponseEntity.ok(ApiUtils.success(adminService.findMemberChange(centerCode, year, userCodes)));
    }

    // 입탈퇴 조회 선생님 선택 목록
    @GetMapping("/center/member-change/teachers")
    public ResponseEntity<?> getMemberChangeTeachers(@RequestParam("centerCode") String centerCode,
                                                     @RequestParam("year") String year) {
        return ResponseEntity.ok(ApiUtils.success(adminService.findMemberChangeTeacherList(centerCode, year)));
    }

    // 입탈퇴 조회 "행사 or 이슈" : 조회 / 저장(빈 값이면 삭제)
    @GetMapping("/center/member-change/events")
    public ResponseEntity<?> getMemberChangeEvents(@RequestParam("centerCode") String centerCode,
                                                   @RequestParam("year") String year) {
        return ResponseEntity.ok(ApiUtils.success(adminService.findMemberChangeEvents(centerCode, year)));
    }

    @PostMapping("/center/member-change/event")
    public ResponseEntity<?> saveMemberChangeEvent(@RequestBody AdminReqDTO.MemberChangeEventDTO dto,
                                                   HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        log.info("행사 or 이슈 저장 요청 : {} (user={})", dto, user == null ? null : user.getUserCode());
        if (user == null)
            return ResponseEntity.status(401).body(ApiUtils.error("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED));
        try {
            adminService.saveMemberChangeEvent(dto, user.getUserCode());
            return ResponseEntity.ok(ApiUtils.success("저장되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("행사 or 이슈 저장 거절 : {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    // 입탈퇴 조회 검증 : 시간표 조회 화면 숫자와 다른 칸 목록 (빈 목록이면 일치)
    @GetMapping("/center/member-change/verify")
    public ResponseEntity<?> verifyMemberChange(
            @RequestParam(value = "centerCode") String centerCode,
            @RequestParam(value = "year") String year) {
        return ResponseEntity.ok(ApiUtils.success(adminService.verifyMemberChange(centerCode, year)));
    }

    @PostMapping("/order/deadline")
    @ResponseBody
    public ResponseEntity<?> updateOrderDeadline(@RequestBody AdminReqDTO.OrderDeadlineDTO dto) {
        adminService.updateOrderDeadline(dto);
        return ResponseEntity.ok(ApiUtils.success("success"));
    }

    // 매출자료(여신금융/홈택스) ↔ 결제내역 승인번호 대조 (센터 선택)
    @PostMapping("/reconcile")
    public ResponseEntity<?> reconcile(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "source", defaultValue = "yeoshin") String source,
                                       @RequestParam("centerCode") String centerCode,
                                       HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        if (centerCode == null || centerCode.isBlank()) {
            return ResponseEntity.ok(Map.of("success", false, "msg", "센터를 선택해주세요."));
        }
        try {
            ReconcileDTO.Result result = paymentReconcileService.reconcile(file, source, centerCode);
            return ResponseEntity.ok(ApiUtils.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "msg", e.getMessage()));
        } catch (Exception e) {
            log.error("승인번호 대조 실패", e);
            return ResponseEntity.ok(Map.of("success", false, "msg", "대조 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/reconcile/download")
    public ResponseEntity<byte[]> downloadReconcile(@RequestBody ReconcileDTO.ExportRequest req,
                                                    HttpSession session) throws IOException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        byte[] excel = paymentReconcileService.exportResult(req.getResult());
        String filename = URLEncoder.encode("매출대조_불일치내역.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
