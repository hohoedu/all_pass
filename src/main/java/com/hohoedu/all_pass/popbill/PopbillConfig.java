package com.hohoedu.all_pass.popbill;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopbillConfig {

    // 센터별 값
    private String corpNum;
    private String userId;
    private String senderNumber;

    // 공통 값
    private String linkId;
    private String secretKey;
    private boolean isTest;
    private boolean isIpRestrictOnOff;
    private boolean useStaticIp;
    private boolean useLocalTimeYn;
}