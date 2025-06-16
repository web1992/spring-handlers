package com.gbdmf.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    public SseEmitter createConnection(String clientId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30分钟超时
        clients.put(clientId, emitter);

        emitter.onCompletion(() -> clients.remove(clientId));
        emitter.onTimeout(() -> clients.remove(clientId));
        emitter.onError(e -> clients.remove(clientId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("连接成功"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emitter;
    }

    public String sendMessage(String clientId, String message) {
        SseEmitter emitter = clients.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("message").data(message));
                return "消息已发送";
            } catch (IOException e) {
                clients.remove(clientId);
                return "发送失败：" + e.getMessage();
            }
        }
        return "客户端未连接";
    }

    public void removeClient(String clientId) {
        clients.remove(clientId);
    }
}
