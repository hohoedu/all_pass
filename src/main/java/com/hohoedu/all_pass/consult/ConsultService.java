package com.hohoedu.all_pass.consult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import com.hohoedu.all_pass.consult.model.InflowRoute;
import com.hohoedu.all_pass.consult.repository.ConsultRepository;
import com.hohoedu.all_pass.consult.repository.InflowRouteJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

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

        consultRepository.registerConsult(reqDTO);
    }

    public List<ConsultRespDTO.ConsultDTO> findConsult(String centerCode, String userCode) {
        List<ConsultRespDTO.ConsultDTO> response = consultRepository.findAll(centerCode, userCode);
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm:ss[.S]]");

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

        response.stream()
                .forEach(dto -> {
                    String sendAt = dto.getSendAt();
                    if (sendAt == null || sendAt.isBlank()) return;

                    LocalDate date = LocalDate.parse(sendAt, inputFormatter);
                    dto.setSendAt(date.format(outputFormatter));
                });
        return response;
    }

    public List<ConsultRespDTO.ConsultDTO> findByPeriod(String startYm, String endYm, String centerCode, String userCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("startYm", startYm);
        params.put("endYm", endYm);
        params.put("centerCode", centerCode);
        params.put("userCode", userCode);
        List<ConsultRespDTO.ConsultDTO> response = consultRepository.findByPeriod(params);
        DateTimeFormatter inputFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm:ss[.S]]");

        DateTimeFormatter outputFormatter =
                DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

        response.stream()
                .forEach(dto -> {
                    String sendAt = dto.getSendAt();
                    if (sendAt == null || sendAt.isBlank()) return;

                    LocalDate date = LocalDate.parse(sendAt, inputFormatter);
                    dto.setSendAt(date.format(outputFormatter));
                });
        return response;
    }

    public void deleteByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        consultRepository.deleteByIds(ids);
    }

    public void updateProgress(Integer id, String progressKey) {
        consultRepository.updateProgress(id, progressKey);
    }

    public void updateConsult (ConsultReqDTO.ConsultRegisterReqDTO reqDTO) {
        consultRepository.updateConsult(reqDTO);
    }
}
