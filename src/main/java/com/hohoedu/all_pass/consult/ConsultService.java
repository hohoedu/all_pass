package com.hohoedu.all_pass.consult;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    public List<ConsultRespDTO.ConsultDTO> findConsult(ConsultReqDTO.ConsultListReqDTO reqDTO) {
        if (reqDTO.getProgress().equals("all")) {
            reqDTO.setProgress(null);
        }
        List<ConsultRespDTO.ConsultDTO> response = consultRepository.findConsult(reqDTO);

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

    public List<ConsultRespDTO.ConsultDTO> findByPeriod(String startDate, String endDate, String centerCode, String userCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("centerCode", centerCode);
        params.put("userCode", userCode);
        List<ConsultRespDTO.ConsultDTO> response = consultRepository.findByPeriod(params);
        log.info(response.toString());
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

    @Transactional
    public int updateConsultContent(Integer id, String content) {
        int result = consultRepository.updateConsultContent(id, content);
//        if(result > 0) {
//            return consultRepository.
//        }
        return result;
    }

    public void updateConsult(ConsultReqDTO.ConsultRegisterReqDTO reqDTO) {
        consultRepository.updateConsult(reqDTO);
    }

    public List<ConsultRespDTO.ConsultPrintDTO> findConsultForPrint(String userCode, String startDate, String endDate) {

        Map<String, Integer> progressOrder = Map.of(
                "confirmed", 1,
                "waiting", 2,
                "counseling", 3,
                "ended", 4
        );

        return consultRepository.findConsultForPrint(userCode, startDate, endDate)
                .stream()
                .sorted(Comparator.comparingInt(dto -> {
                    String key = dto.getProgressKey();
                    return key != null ? progressOrder.getOrDefault(key, 99) : 99;
                }))
                .collect(Collectors.toList());
    }

    public String getUserName(String userCode) {
        return consultRepository.findUserNameByUserCode(userCode);
    }


    public void updateSendKey(String consultId, String sendKey) {
        consultRepository.updateSendKey(consultId, sendKey);
    }
}
