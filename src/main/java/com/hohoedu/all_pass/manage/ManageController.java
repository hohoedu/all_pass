package com.hohoedu.all_pass.manage;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageController {

    private final UserService userService;
    private final ManageService manageService;


    @PostMapping("/insert")
    public ResponseEntity<?> insertClassFeeMap(@RequestBody ManageReqDTO.InsertClassFeeDTO reqDTO) {

        if (reqDTO.getClassFeeMap() == null || reqDTO.getClassFeeMap().isEmpty()) {
            return ResponseEntity.badRequest().body("등록할 데이터가 없습니다.");
        }

        int inserted = manageService.updateClassFeeMap(reqDTO.getClassFeeMap());

        return ResponseEntity.ok(ApiUtils.success("저장되었습니다."));
    }
}
