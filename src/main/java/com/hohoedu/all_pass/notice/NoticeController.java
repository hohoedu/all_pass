package com.hohoedu.all_pass.notice;

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

import java.util.Map;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final FileUploadService fileUploadService;

    @PostMapping("/center/save")
    public ResponseEntity<?> insertCenterNotice(@RequestBody NoticeReqDTO.CenterNoticeSaveReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        noticeService.insertCenterNotice(reqDTO, user);
        return ResponseEntity.ok(ApiUtils.success("등록 완료"));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> noticeImageUpload(@RequestParam("file") MultipartFile file) {
        String savedPath = fileUploadService.uploadNotice(file);

        return ResponseEntity.ok(savedPath);
    }

    @PostMapping("/detail")
    public ResponseEntity<?> centerNoticeDetail(@RequestBody Map<String, Integer> noticeId, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        NoticeRespDTO.CenterNoticeDetailDTO response = noticeService.findCenterNoticeByNoticeId(user, noticeId.get("id"));
        return ResponseEntity.ok(ApiUtils.success(response));
    }


}
