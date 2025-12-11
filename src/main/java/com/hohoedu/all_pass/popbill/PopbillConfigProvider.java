package com.hohoedu.all_pass.popbill;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopbillConfigProvider {

    private final PopbillProperties properties;

    public PopbillConfig getConfig(String centerCode) {

        PopbillCenterProperties centerProps =
                properties.getCenters().get(centerCode);

        if (centerProps == null) {
            throw new IllegalArgumentException("Popbill 설정을 찾을 수 없습니다. centerCode=" + centerCode);
        }

        return new PopbillConfig(
                centerProps.getCorpNum(),
                centerProps.getUserId(),
                centerProps.getSenderNumber(),

                properties.getLinkId(),
                properties.getSecretKey(),
                properties.isTest(),
                properties.isIpRestrictOnOff(),
                properties.isUseStaticIp(),
                properties.isUseLocalTimeYn()
        );
    }
}