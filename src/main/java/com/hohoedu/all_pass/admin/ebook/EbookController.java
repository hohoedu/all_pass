package com.hohoedu.all_pass.admin.ebook;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.admin.ebook._dto.EbookReqDTO;
import com.hohoedu.all_pass.admin.ebook.model.PersonYear;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(ApiUtils.success("hello"));
    }

    @PostMapping("/load/person")
    public ResponseEntity<EbookReqDTO.PersonSettingDTO> loadPersonSetting(@RequestBody EbookReqDTO.PersonFindDTO reqDTO) {

        EbookReqDTO.PersonSettingDTO result = ebookService.loadPersonYear(reqDTO);
        return ResponseEntity.ok(result);
    }

}
