package com.hohoedu.all_pass.admin.ebook;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.admin.ebook._dto.EbookReqDTO;
import com.hohoedu.all_pass.admin.ebook.model.PersonYear;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EbookController {

    private final EbookService ebookService;

    @PostMapping("/save/person")
    public ResponseEntity<?> createPersonYear(@RequestBody EbookReqDTO.PersonSettingDTO reqDTO) {
        int response = ebookService.insertPersonYear(reqDTO);
        if (response > 0) {
            return ResponseEntity.ok(ApiUtils.success("저장되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(ApiUtils.error("저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/load/person")
    public ResponseEntity<EbookReqDTO.PersonSettingDTO> loadPersonSetting(@RequestBody EbookReqDTO.PersonFindDTO reqDTO) {

        EbookReqDTO.PersonSettingDTO result = ebookService.loadPersonYear(reqDTO);
        return ResponseEntity.ok(result);
    }

}
