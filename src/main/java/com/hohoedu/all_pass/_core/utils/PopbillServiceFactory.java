package com.hohoedu.all_pass._core.utils;

import com.hohoedu.all_pass.popbill.PopbillConfig;
import com.hohoedu.all_pass.popbill.repository.PopbillRepository;
import com.popbill.api.BaseService;
import com.popbill.api.KakaoService;
import com.popbill.api.PopbillException;
import com.popbill.api.kakao.KakaoServiceImp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopbillServiceFactory {

    private final PopbillRepository popbillRepository;

    @Value("${popbill.isTest}")
    private boolean isTest;

    @Value("${popbill.isIpRestrictOnOff}")
    private boolean isIpRestrict;

    @Value("${popbill.useStaticIp}")
    private boolean useStaticIp;

    @Value("${popbill.useLocalTimeYn}")
    private boolean useLocalTimeYn;

    @Value("${popbill.aes.key}")
    private String aesKey;

    public KakaoService getKakaoService(String centerCode) {

        PopbillConfig config = popbillRepository.findPopbillConfig(centerCode);

        String secretKey = Aes256Util.decrypt(config.getEncryptedKey(), aesKey);

        KakaoServiceImp kakaoService = new KakaoServiceImp();
        kakaoService.setLinkID(config.getLinkId());
        kakaoService.setSecretKey(secretKey);
        kakaoService.setTest(isTest);
        kakaoService.setIPRestrictOnOff(isIpRestrict);
        kakaoService.setUseStaticIP(useStaticIp);
        kakaoService.setUseLocalTimeYN(useLocalTimeYn);

        return kakaoService;
    }

    public BaseService getBaseService(String centerCode) {
        PopbillConfig popbillConfig = popbillRepository.findPopbillConfig(centerCode);
        String secretKey = Aes256Util.decrypt(popbillConfig.getEncryptedKey(), aesKey);

        KakaoServiceImp baseService = new KakaoServiceImp();
        baseService.setLinkID(popbillConfig.getLinkId());
        baseService.setSecretKey(secretKey);
        baseService.setTest(isTest);

        return baseService;
    }

    public String getChargeUrl(String centerCode, String userId) throws PopbillException {

        PopbillConfig popbillConfig = popbillRepository.findPopbillConfig(centerCode);
        String secretKey = Aes256Util.decrypt(popbillConfig.getEncryptedKey(), aesKey);
        KakaoServiceImp baseService = new KakaoServiceImp();
        baseService.setLinkID(popbillConfig.getLinkId());
        baseService.setSecretKey(secretKey);
        baseService.setTest(isTest);

        return baseService.getChargeURL(centerCode, userId);
    }
}
