package com.hohoedu.all_pass.manage;

import com.google.api.Http;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageController {

    private final UserService userService;
    private final ManageService manageService;


    @PostMapping("/order/save")
    public ResponseEntity<?> insertOrder(@RequestBody ManageReqDTO.InsertOrderHistoryDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        reqDTO.setCenterCode(user.getCenterCode());
        reqDTO.setUserCode(user.getUserCode());

        manageService.insertOrder(reqDTO);

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/order/list")
    public ResponseEntity<?> getSavedOrderList(@RequestBody ManageReqDTO.GetOrderDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<ManageRespDTO.SavedOrderListDTO> orderList = manageService.getSavedOrderList(user.getUserCode(), user.getCenterCode(), reqDTO.getYy(), reqDTO.getMm());

        return ResponseEntity.ok(ApiUtils.success(orderList));
    }

    @PostMapping("/order/base/list")
    public ResponseEntity<?> getBasedOrderList(@RequestBody ManageReqDTO.GetOrderDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<ManageRespDTO.BasicOrderListDTO> orderList = manageService.getBasicOrderList(user.getUserCode(), user.getCenterCode(), reqDTO.getYy(), reqDTO.getMm());

        return ResponseEntity.ok(ApiUtils.success(orderList));
    }

    @PostMapping("/order/detail/list")
    public ResponseEntity<?> getOrderDetailList(@RequestBody ManageReqDTO.GetOrderDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<ManageRespDTO.OrderDetailDTO> response = manageService.getOrderDetailList(user.getUserCode(), user.getCenterCode(), reqDTO.getYy(), reqDTO.getMm());
        log.info(response.toString());
        return ResponseEntity.ok(ApiUtils.success(response));
    }


    @PostMapping("/reorder/save")
    public ResponseEntity<?> insertReorder(@RequestBody ManageReqDTO.InsertReorderDTO reqDTO, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        int result = manageService.insertReorder(reqDTO, user);

        if (result > 0) {
            return ResponseEntity.ok(ApiUtils.success("저장되었습니다."));
        } else {
            return ResponseEntity.ok(ApiUtils.error("저장을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }

    }

    @PostMapping("/reorder/list")
    public ResponseEntity<?> getreOrderList(@RequestBody ManageReqDTO.GetOrderDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        log.info("reqDTO = {}", reqDTO);
        List<ManageRespDTO.ReorderListDTO> reorderList = manageService.getReorderList(user.getUserCode(), user.getCenterCode(), reqDTO.getYy(), reqDTO.getMm());

        return ResponseEntity.ok(ApiUtils.success(reorderList));
    }


    @PostMapping("/reorder/cancel")
    public ResponseEntity<?> cancelReorder(@RequestBody ManageReqDTO.CancelReorderDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        String result = manageService.cancelReorder(dto.getId());
        log.info("result = {}", result);
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/fee/insert")
    public ResponseEntity<?> insertClassFeeMap(@RequestBody ManageReqDTO.InsertClassFeeDTO reqDTO, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        if (reqDTO.getClassFeeMap() == null || reqDTO.getClassFeeMap().isEmpty()) {
            return ResponseEntity.badRequest().body("등록할 데이터가 없습니다.");
        }

        int response = manageService.insertClassFeeMap(reqDTO.getClassFeeMap(), user.getCenterCode());
        if (response == 0) {
            return ResponseEntity.ok(ApiUtils.error("저장되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        } else {
            return ResponseEntity.ok(ApiUtils.success("저장되었습니다."));
        }
    }


}
