package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass._core.utils.FileUploadService;
import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FileUploadService fileUploadService;

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

    @PostMapping("/order/deadline")
    @ResponseBody
    public ResponseEntity<?> updateOrderDeadline(@RequestBody AdminReqDTO.OrderDeadlineDTO dto) {
        adminService.updateOrderDeadline(dto);
        return ResponseEntity.ok(ApiUtils.success("success"));
    }
}
