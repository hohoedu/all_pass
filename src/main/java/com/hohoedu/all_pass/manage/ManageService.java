package com.hohoedu.all_pass.manage;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageService {

    private final PaymentRepository paymentRepository;

    public int updateClassFeeMap(List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList) {
        return paymentRepository.updateClassFeeMap(feeMapList);
    }
}
