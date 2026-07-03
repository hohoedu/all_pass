package com.hohoedu.all_pass._core.firebase;

import com.google.firebase.messaging.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class FcmService {

    @Getter
    public static class SendProgress {
        private final int total;
        private final AtomicInteger sent = new AtomicInteger(0);
        private volatile boolean done = false;

        public SendProgress(int total) {
            this.total = total;
        }
    }

    private final Map<String, SendProgress> progressMap = new ConcurrentHashMap<>();

    /**
     * 토큰 목록에 비동기로 알림을 발송하고, key로 진행률을 조회할 수 있게 한다.
     */
    public void sendMessagesAsync(String key, List<String> tokens, String title, String body) {
        SendProgress progress = new SendProgress(tokens.size());
        progressMap.put(key, progress);

        CompletableFuture.runAsync(() -> {
            try {
                for (String token : tokens) {
                    try {
                        sendMessage(token, title, body);
                    } catch (Exception e) {
                        log.error("FCM 발송 실패 (token: {}): {}", token, e.getMessage());
                    }
                    progress.getSent().incrementAndGet();
                }
            } finally {
                progress.done = true;
                // 완료 후 5분 뒤 진행률 정보 제거
                CompletableFuture.delayedExecutor(5, java.util.concurrent.TimeUnit.MINUTES)
                        .execute(() -> progressMap.remove(key));
            }
        });
    }

    public SendProgress getProgress(String key) {
        return progressMap.get(key);
    }

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
