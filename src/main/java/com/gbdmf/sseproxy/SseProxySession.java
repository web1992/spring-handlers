//package com.gbdmf.sseproxy;
//
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.sse.EventSource;
//
//import javax.servlet.AsyncContext;
//
//@Slf4j
//@Getter
//public class SseProxySession {
//
//    private final AsyncContext asyncContext;
//    private final EventSource eventSource;
//    private final long createTime; // 时间戳：毫秒
//    private long updateTime; // 时间戳：毫秒
//
//    public SseProxySession(AsyncContext asyncContext, EventSource eventSource) {
//        this.asyncContext = asyncContext;
//        this.eventSource = eventSource;
//        this.createTime = System.currentTimeMillis();
//        this.updateTime = System.currentTimeMillis();
//    }
//
//    public void refresh() {
//        this.updateTime = System.currentTimeMillis();
//    }
//
//    public void close() {
//        try {
//            if (eventSource != null) {
//                eventSource.cancel();
//            }
//        } catch (Exception e) {
//            log.warn("SseProxySession#close eventSource sse session failed", e);
//        }
//        try {
//            if (asyncContext != null) {
//                asyncContext.complete();
//            }
//        } catch (Exception e) {
//            log.warn("SseProxySession#close asyncContext sse session failed", e);
//        }
//    }
//}
