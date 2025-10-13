package com.hohoedu.all_pass.consult;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;

import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    @PostMapping("/save")
    public String getMethodName(@RequestBody ConsultReqDTO.ConsultRegisterReqDTO reqDTO) {
        System.out.println("gradeNo = " + reqDTO.getGradeKey());
        System.out.println("inflowRouteNo = " + reqDTO.getInflowRouteKey());
        System.out.println(reqDTO.getStudentName());
        consultService.registerConsult(reqDTO);
        return "redirect:/consult/main";
    }

    @PostMapping("/search")
    @ResponseBody
    public List<ConsultRespDTO.ConsultDTO> searchByPeriod(@RequestBody Map<String, String> req) {
        String startYm = req.get("startYm");
        String endYm = req.get("endYm");
        return consultService.findByPeriod(startYm, endYm);
    }

    @PostMapping("/delete")
    @ResponseBody
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
    @ResponseBody
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

}
