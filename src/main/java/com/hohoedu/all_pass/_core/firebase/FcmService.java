package com.hohoedu.all_pass._core.firebase;

import com.google.firebase.messaging.*;

import com.hohoedu.all_pass.app.AppRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    // 앱 알림 종류 — erp_app_noti_type.type_code 와 같고, FCM data.type 으로도 앱에 간다
    public static final String TYPE_NOTICE = "NOTICE";
    public static final String TYPE_LESSON = "LESSON";
    public static final String TYPE_CLASS_RESULT = "CLASS_RESULT";
    public static final String TYPE_ATTENDANCE = "ATTENDANCE";
    public static final String TYPE_CLASS_BOOK = "CLASS_BOOK";
    public static final String TYPE_MONTH_EVAL = "MONTH_EVAL";
    public static final String TYPE_READING_CLINIC = "READING_CLINIC";

    private final AppRepository appRepository;

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
    public void sendMessagesAsync(String key, List<String> tokens, String type, String title, String body) {
        SendProgress progress = new SendProgress(tokens.size());
        progressMap.put(key, progress);

        CompletableFuture.runAsync(() -> {
            try {
                for (String token : tokens) {
                    try {
                        sendMessage(token, type, title, body);
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

    /**
     * 받는 학생이 이 종류의 앱 알림을 꺼 두었으면 보내지 않는다.
     * 그래도 true 를 돌려주므로 호출하는 쪽의 발송 처리(is_send 등)는 그대로다 — 선생님에게는 알리지 않는다.
     */
    public boolean sendMessage(String token, String type, String title, String body) {
        if (isMuted(token, type)) {
            log.info("앱 알림 수신 거부로 미발송 (type: {})", type);
            return true;
        }
        try {
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("high_importance_channel_v3")
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
                    .putData("type", type)
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig);

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // 조회가 실패하면(테이블 미생성 등) 보내는 쪽으로 둔다
    private boolean isMuted(String token, String type) {
        try {
            return appRepository.isNotiMuted(token, type);
        } catch (Exception e) {
            log.error("알림 수신 설정 조회 실패: {}", e.getMessage());
            return false;
        }
    }
}
