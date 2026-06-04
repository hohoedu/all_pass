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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    // 상담 문의 기록 조회
    @PostMapping("/search")
    public ResponseEntity<?> searchConsult(@RequestBody ConsultReqDTO.ConsultListReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        reqDTO.setCenterCode(user.getCenterCode());
        List<ConsultRespDTO.ConsultDTO> list = consultService.findConsult(reqDTO);

        return ResponseEntity.ok(ApiUtils.success(list));
    }

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

    @PostMapping("/modal/search")
    public ResponseEntity<?> searchModalConsult(@RequestBody ConsultReqDTO.ConsultModalReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        reqDTO.setCenterCode(user.getCenterCode());
        List<ConsultRespDTO.ConsultModalRespDTO> consultList = consultService.findConsultModal(reqDTO);
        return ResponseEntity.ok(ApiUtils.success(consultList));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateConsult(@RequestBody ConsultReqDTO.ConsultRegisterReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/login").build();

        reqDTO.setCenterCode(user.getCenterCode());
        reqDTO.setUserCode(user.getUserCode());
        consultService.updateConsult(reqDTO);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/update-progress")
    public ResponseEntity<?> updateProgress(@RequestBody ConsultReqDTO.ConsultUpdateProgressReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        reqDTO.setUserCode(user.getUserCode());
        consultService.updateProgress(reqDTO);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/update-memo")
    public ResponseEntity<?> updateMemo(@RequestBody ConsultReqDTO.ConsultMemoUpdateReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/login").build();

        consultService.updateConsultContent(reqDTO.getId(), reqDTO.getContent());
        return ResponseEntity.ok(ApiUtils.success(null));
    }


}
