package com.hohoedu.all_pass._core.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FcmService {
    public boolean sendMessage(String token, String title, String body) {
        try {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            String response = FirebaseMessaging.getInstance().send(builder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
