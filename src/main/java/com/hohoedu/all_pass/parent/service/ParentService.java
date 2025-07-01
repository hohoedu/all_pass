package com.hohoedu.all_pass.parent.service;

import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.parent.repository.ParentRepository;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;

    public void parentInsert(StudentReqDTO.ParentJoinDTO requestDTO) {

        parentRepository.insert(requestDTO);
    }
}
