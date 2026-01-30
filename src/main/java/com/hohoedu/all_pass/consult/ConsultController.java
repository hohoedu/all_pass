package com.hohoedu.all_pass.consult;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;

import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    @PostMapping("/save")
    public ResponseEntity<?> createConsult(@RequestBody ConsultReqDTO.ConsultRegisterReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        reqDTO.setUserCode(user.getUserCode());
        reqDTO.setCenterCode(user.getCenterCode());
        consultService.registerConsult(reqDTO);
        return ResponseEntity.ok(ApiUtils.success("success"));
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchByPeriod(@RequestBody ConsultReqDTO.GetConsultReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        String startYm = req.getStartYm();
        String endYm = req.getEndYm();

        List<ConsultRespDTO.ConsultDTO> response = consultService.findByPeriod(startYm, endYm, user.getCenterCode(), req.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteConsults(@RequestBody List<Integer> ids) {
        try {
            consultService.deleteByIds(ids);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("삭제 실패: " + e.getMessage());
        }
    }

    @PostMapping("/update-progress")
    public ResponseEntity<String> updateProgress(@RequestBody Map<String, Object> req) {
        try {
            Integer id = Integer.valueOf(req.get("id").toString());
            String progressKey = req.get("progressKey").toString();

            consultService.updateProgress(id, progressKey);
            return ResponseEntity.ok("진행상황 변경 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("진행상황 변경 실패: " + e.getMessage());
        }
    }

    @PostMapping("/content-update")
    public ResponseEntity<?> updateConsultContent(@RequestBody ConsultReqDTO.ConsultUpdateContentDTO dto) {

        int result = consultService.updateConsultContent(dto.getConsultId(), dto.getContent());

        return ResponseEntity.ok(ApiUtils.success(null));

    }

    @PostMapping("/update")
    public ResponseEntity<?> findConsultDetail(@RequestBody ConsultReqDTO.ConsultRegisterReqDTO req) {
        consultService.updateConsult(req);
        return ResponseEntity.ok(ApiUtils.success("response"));
    }

}
