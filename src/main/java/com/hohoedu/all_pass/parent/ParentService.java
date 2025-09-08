package com.hohoedu.all_pass.parent;

import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.family.repository.ParentRepository;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;


}
