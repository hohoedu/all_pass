package com.hohoedu.all_pass._core.utils;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterHolder {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void add(String jobId, SseEmitter emitter) {
        emitters.put(jobId, emitter);
    }

    public void send(String jobId, Object data) {
        SseEmitter emitter = emitters.get(jobId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(data, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            emitters.remove(jobId);
        }
    }

    public void complete(String jobId) {
        SseEmitter emitter = emitters.get(jobId);
        if (emitter == null) return;
        try {
            emitter.complete();
        } catch (Exception ignored) {}
        emitters.remove(jobId);
    }

    public void remove(String jobId) {
        emitters.remove(jobId);
    }
}
