//package com.gbdmf.sseproxy;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//@Slf4j
//public class SseProxySessionManager {
//
//    // 使用用户 ID、sessionId、IP 等作为 key
//    private final ConcurrentHashMap<String, SseProxySession> sessions = new ConcurrentHashMap<>();
//
//    public Map<String, SseProxySession> getSessions() {
//        return Collections.unmodifiableMap(sessions);
//    }
//
//    public void addSession(String key, SseProxySession session) {
//        // 如果已存在，关闭旧的
//        SseProxySession old = sessions.put(key, session);
//        if (old != null) {
//            old.close();
//        }
//    }
//
//    public void refresh(String key) {
//        SseProxySession session = sessions.get(key);
//        if (session != null) {
//            session.refresh();
//        }
//    }
//
//    public void removeSession(String key) {
//        SseProxySession session = sessions.remove(key);
//        if (session != null) {
//            try {
//                session.close();
//            } catch (Exception e) {
//                log.warn("SseProxySessionManager#removeSession sessionId={} failed", key, e);
//            }
//        }
//    }
//
//    public void closeAll() {
//        for (Map.Entry<String, SseProxySession> entry : sessions.entrySet()) {
//            entry.getValue().close();
//        }
//        sessions.clear();
//    }
//}
