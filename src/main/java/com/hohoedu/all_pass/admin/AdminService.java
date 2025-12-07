package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.BookSuggest;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    public int insertPersonYear(AdminReqDTO.PersonSettingDTO reqDTO) {
        int count = 0;
        for (AdminReqDTO.PersonSettingDTO.EbookClassDTO cls : reqDTO.getClasses()) {
            String classKey = cls.getClass_key();

            for (AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m : cls.getMonths()) {

                count += adminRepository.insertPersonYear(
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

    public AdminReqDTO.PersonSettingDTO loadPersonYear(AdminReqDTO.PersonFindDTO req) {

        List<AdminRespDTO.PersonYearDTO> list = adminRepository.selectPersonYear(req.getCenterCode(), req.getYear());

        if (list == null || list.isEmpty()) {
            return null;
        }

        Map<String, List<AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO>> grouped =
                list.stream()
                        .collect(Collectors.groupingBy(
                                AdminRespDTO.PersonYearDTO::getClassKey,
                                LinkedHashMap::new,
                                Collectors.mapping(row -> {
                                    AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m =
                                            new AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO();
                                    m.setMonth(row.getMm());
                                    m.setUnit_key(row.getUnitKey());
                                    m.setSub_unit_key(row.getSubUnitKey());
                                    return m;
                                }, Collectors.toList())
                        ));

        List<AdminReqDTO.PersonSettingDTO.EbookClassDTO> classes =
                grouped.entrySet().stream()
                        .map(e -> {
                            AdminReqDTO.PersonSettingDTO.EbookClassDTO c =
                                    new AdminReqDTO.PersonSettingDTO.EbookClassDTO();
                            c.setClass_key(e.getKey());
                            c.setMonths(e.getValue());
                            return c;
                        })
                        .collect(Collectors.toList());

        // 3) 최종 DTO 구성
        AdminReqDTO.PersonSettingDTO dto = new AdminReqDTO.PersonSettingDTO();
        dto.setCenterCode(req.getCenterCode());
        dto.setYear(req.getYear());
        dto.setClasses(classes);

        return dto;
    }


    public void saveBookSuggest(AdminReqDTO.BookSuggestSaveReqDTO req) {

        for (AdminReqDTO.BookSuggestSaveReqDTO.WeekDTO w : req.getWeeks()) {

            AdminReqDTO.BookSuggestDTO dto = new AdminReqDTO.BookSuggestDTO();
            dto.setClassKey(req.getClassKey());
            dto.setYy(req.getYy());
            dto.setMm(req.getMm());
            dto.setWeek(w.getWeek());

            dto.setSubjectKey(w.getSubjectKey());
            dto.setBookName(w.getBookName());
            dto.setPublisher(w.getPublisher());
            dto.setBookImageUrl(w.getImageUrl());

            adminRepository.upsertBookSuggest(dto);
        }
    }

    public List<SubjectCode> findSubjects() {

        return adminRepository.findSubject();
    }

}