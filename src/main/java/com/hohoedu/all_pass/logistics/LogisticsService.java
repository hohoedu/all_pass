package com.hohoedu.all_pass.logistics;

import com.hohoedu.all_pass.logistics._dto.LogisReqDTO;
import com.hohoedu.all_pass.logistics._dto.LogisRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsRepository logisticsRepository;

    public List<LogisRespDTO.ReorderListDTO> findReorderList(LogisReqDTO.ReorderListReqDTO req) {
        return logisticsRepository.findReorderList(req.getYear(), req.getMonth(), req.getCenterCode(), req.isOnlyWait());
    }
}
