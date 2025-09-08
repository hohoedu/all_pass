package com.hohoedu.all_pass.family;

import com.hohoedu.all_pass.family.repository.ParentRepository;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final ParentRepository parentRepository;

    public void parentInsert(StudentReqDTO.ParentJoinDTO requestDTO) {

        parentRepository.insert(requestDTO);
    }
}
