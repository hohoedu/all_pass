package com.hohoedu.all_pass.admin.center;

import com.hohoedu.all_pass.admin.center.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class CenterService {

    private final CenterRepository centerRepository;

    public List<Center> findAllCenter() {
        List<Center> center = centerRepository.findAll();
        return center;
    }

}
