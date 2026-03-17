package com.hohoedu.all_pass.family;

import com.hohoedu.all_pass.family.repository.FamilyRepository;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final FamilyRepository familyRepository;

    public void parentInsert(StudentWebReqDTO.ParentJoinDTO requestDTO) {

        familyRepository.insert(requestDTO);
    }

    public void processSibling(String studentId, String centerCode, String telFirst, String telMiddle, String telLast) {

        String groupKey = telFirst + telMiddle + telLast + "_" + centerCode;

        // 동일 그룹 학생 수 확인 (본인 포함, 2~4명만 처리)
        int siblingCount = familyRepository.countSiblingGroup(groupKey);

        if (siblingCount < 2 || siblingCount > 4) {
            return;
        }

        // 기존 sibling_key 조회
        Integer existingSiblingKey = familyRepository.findSiblingKey(groupKey);

        if (existingSiblingKey == null) {

            int newSiblingKey = familyRepository.getMaxSiblingKey() + 1;
            familyRepository.insertSiblingGroup(groupKey, newSiblingKey, centerCode);
        } else {

            familyRepository.insertSibling(existingSiblingKey, studentId, centerCode);
        }
    }
}
