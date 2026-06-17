package com.hohoedu.all_pass._core.firebase;

import com.google.firebase.messaging.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {
    public boolean sendMessage(String token, String title, String body) {
        try {
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("high_importance_channel_v2")
                            .build())
                    .build();
            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .build())
                    .build();

            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig);

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
