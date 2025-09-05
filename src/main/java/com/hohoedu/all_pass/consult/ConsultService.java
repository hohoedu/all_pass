package com.hohoedu.all_pass.consult;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import com.hohoedu.all_pass.consult.model.InflowRoute;
import com.hohoedu.all_pass.consult.repository.ConsultRepository;
import com.hohoedu.all_pass.consult.repository.InflowRouteJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ConsultService {

    private final InflowRouteJpaRepository inflowRouteJpaRepository;
    private final ConsultRepository consultRepository;

    public List<InflowRoute> findInflowRoute() {
        List<InflowRoute> routes = inflowRouteJpaRepository.findAll();
        return routes;
    }

    public void registerConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO) {
        System.out.println("호출");
        consultRepository.registerConsult(reqDTO);
    }

    public List<ConsultRespDTO.ConsultDTO> findConsult(){
        
        return consultRepository.findAll();
    }

}
