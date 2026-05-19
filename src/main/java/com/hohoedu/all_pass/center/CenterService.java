package com.hohoedu.all_pass.center;

import com.hohoedu.all_pass.center._dto.CenterRespDTO.PaymintConfigDTO;
import com.hohoedu.all_pass.center._dto.CenterRespDTO.PaymintRemainRespDTO;
import com.hohoedu.all_pass.center.repository.CenterJpaRepository;
import com.hohoedu.all_pass.center.repository.CenterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class CenterService {

    private final CenterJpaRepository centerJpaRepository;
    private final CenterRepository centerRepository;

    public List<Center> findAllCenter() {
        List<Center> center = centerJpaRepository.findAll();
        return center;
    }

    public int findPaymintPoint(String centerCode) {

        PaymintConfigDTO config = centerRepository.findPaymintConfigByCenterCode(centerCode);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("apiKey", config.getApiKey());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        PaymintRemainRespDTO resp = restTemplate.postForObject(
                config.getRemainPointUrl(),
                request,
                PaymintRemainRespDTO.class);

        if (resp == null || !"0000".equals(resp.getCode())) {
            throw new RuntimeException("Paymint 포인트 조회 실패");
        }

        return resp.getInfo().getRemainCount();
    }

}
