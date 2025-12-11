package com.hohoedu.all_pass.popbill;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "popbill")
public class PopbillProperties {

    private String linkId;
    private String secretKey;
    private boolean isTest;
    private boolean isIpRestrictOnOff;
    private boolean useStaticIp;
    private boolean useLocalTimeYn;

    private Map<String, PopbillCenterProperties> centers = new HashMap<>();
}