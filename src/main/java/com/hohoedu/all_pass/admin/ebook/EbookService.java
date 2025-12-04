package com.hohoedu.all_pass.admin.ebook;

import com.hohoedu.all_pass.admin.ebook._dto.EbookReqDTO;
import com.hohoedu.all_pass.admin.ebook._dto.EbookRespDTO;
import com.hohoedu.all_pass.admin.ebook.model.PersonYear;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class EbookService {
    private final EbookRepository ebookRepository;

    public int insertPersonYear(EbookReqDTO.PersonSettingDTO reqDTO) {
        int count = 0;
        for (EbookReqDTO.PersonSettingDTO.EbookClassDTO cls : reqDTO.getClasses()) {
            String classKey = cls.getClass_key();

            for (EbookReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m : cls.getMonths()) {

                count += ebookRepository.insertPersonYear(
                        reqDTO.getCenterCode(),
                        reqDTO.getYear(),
                        m.getMonth(),
                        classKey,
                        m.getUnit_key(),
                        m.getSub_unit_key()
                );
            }
        }
        return count;
    }

    public EbookReqDTO.PersonSettingDTO loadPersonYear(EbookReqDTO.PersonFindDTO req) {

        List<EbookRespDTO.PersonYearDTO> list = ebookRepository.selectPersonYear(req.getCenterCode(), req.getYear());

        if (list == null || list.isEmpty()) {
            return null;
        }

        Map<String, List<EbookReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO>> grouped =
                list.stream()
                        .collect(Collectors.groupingBy(
                                EbookRespDTO.PersonYearDTO::getClassKey,
                                LinkedHashMap::new,
                                Collectors.mapping(row -> {
                                    EbookReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m =
                                            new EbookReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO();
                                    m.setMonth(row.getMm());
                                    m.setUnit_key(row.getUnitKey());
                                    m.setSub_unit_key(row.getSubUnitKey());
                                    return m;
                                }, Collectors.toList())
                        ));

        List<EbookReqDTO.PersonSettingDTO.EbookClassDTO> classes =
                grouped.entrySet().stream()
                        .map(e -> {
                            EbookReqDTO.PersonSettingDTO.EbookClassDTO c =
                                    new EbookReqDTO.PersonSettingDTO.EbookClassDTO();
                            c.setClass_key(e.getKey());
                            c.setMonths(e.getValue());
                            return c;
                        })
                        .collect(Collectors.toList());

        // 3) 최종 DTO 구성
        EbookReqDTO.PersonSettingDTO dto = new EbookReqDTO.PersonSettingDTO();
        dto.setCenterCode(req.getCenterCode());
        dto.setYear(req.getYear());
        dto.setClasses(classes);

        return dto;
    }

}