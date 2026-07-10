package com.hohoedu.all_pass.notice;

import com.hohoedu.all_pass._core.firebase.FcmService;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass._core.utils.FileUploadService;
import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final FileUploadService fileUploadService;
    private final FcmService fcmService;

    @PostMapping("/center/save")
    public ResponseEntity<?> insertCenterNotice(@RequestBody NoticeReqDTO.CenterNoticeSaveReqDTO reqDTO,
            HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        Map<String, Object> result = noticeService.insertCenterNotice(reqDTO, user);
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @GetMapping("/fcm-progress")
    public ResponseEntity<?> getFcmProgress(@RequestParam("key") String key) {
        FcmService.SendProgress progress = fcmService.getProgress(key);
        Map<String, Object> result = new HashMap<>();
        if (progress == null) {
            // 발송 정보가 없으면 완료로 간주 (서버 재시작 등)
            result.put("done", true);
        } else {
            result.put("total", progress.getTotal());
            result.put("sent", progress.getSent().get());
            result.put("done", progress.isDone());
        }
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/center/update")
    public ResponseEntity<Map<String, Object>> updateNotice(@RequestBody NoticeReqDTO.CenterNoticeUpdateDTO reqDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 필수값 검증
            if (reqDTO.getId() == null) {
                response.put("success", false);
                response.put("message", "수정할 공지 ID가 없습니다.");
                return ResponseEntity.ok(response);
            }

            Map<String, Object> result = noticeService.updateCenterNotice(reqDTO);
            int updated = (int) result.get("updated");

            if (updated > 0) {
                response.put("success", true);
                response.put("message", "공지사항이 수정되었습니다.");
                if (result.containsKey("progressKey")) {
                    response.put("progressKey", result.get("progressKey"));
                    response.put("total", result.get("total"));
                }
            } else {
                response.put("success", false);
                response.put("message", "공지사항 수정에 실패했습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/center/delete")
    public ResponseEntity<Map<String, Object>> deleteNotice(@RequestBody NoticeReqDTO.CenterNoticeDeleteDTO reqDTO,
            HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            int result = noticeService.deleteCenterNotice(reqDTO.getId());

            if (result > 0) {
                response.put("success", true);
                response.put("message", "공지사항이 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "공지사항 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/center/list")
    public ResponseEntity<?> getCenterNoticeList(
            @RequestBody Map<String, String> body, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/login").build();

        boolean myOnly = "true".equals(body.get("myOnly"));
        List<NoticeRespDTO.CenterNoticeDTO> list = noticeService.findCenterNoticeByCenterCode(user, myOnly);
        return ResponseEntity.ok(ApiUtils.success(list));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> noticeImageUpload(@RequestParam("file") MultipartFile file) {
        String savedPath = fileUploadService.uploadNotice(file);

        return ResponseEntity.ok(savedPath);
    }

    @GetMapping("/class-list")
    public ResponseEntity<?> getClassList(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/login").build();
        List<NoticeRespDTO.ClassCodeDTO> list = noticeService.findClassListByCenterCode(user);
        return ResponseEntity.ok(ApiUtils.success(list));
    }

    @PostMapping("/detail")
    public ResponseEntity<?> centerNoticeDetail(@RequestBody Map<String, Integer> noticeId, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        NoticeRespDTO.CenterNoticeDetailDTO response = noticeService.findCenterNoticeByNoticeId(user,
                noticeId.get("id"));
        return ResponseEntity.ok(ApiUtils.success(response));
    }

}
