//package com.gbdmf.sseproxy;
//
//import com.internet.api.biz.entity.ProxyParam;
//import com.internet.api.util.ApiHttpProxyCatchUtil;
//import lombok.extern.slf4j.Slf4j;
//import net.pocrd.entity.CommonParam;
//import okhttp3.OkHttpClient;
//import okhttp3.Protocol;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.sse.EventSource;
//import okhttp3.sse.EventSourceListener;
//import okhttp3.sse.EventSources;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.AsyncContext;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.PrintWriter;
//import java.util.Collections;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@Slf4j
//public class SSEProxyManager {
//
//    private static final String CONTENT_TYPE_EVENT_STREAM = "text/event-stream";
//    private static final String CONTENT_CHARSET = "UTF-8";
//    private static final String HEAD_CACHE_CONTROL = "Cache-Control";
//    private static final String HEAD_CONNECTION = "Connection";
//    private static final String HEAD_NO_CACHE = "no-cache";
//    private static final String HEAD_KEEP_ALIVE = "keep-alive";
//
//    @Autowired
//    private SseProxySessionManager sessionManager;
//
//    OkHttpClient client = new OkHttpClient.Builder()
//            //.protocols(Collections.singletonList(Protocol.HTTP_1_1))
//            .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
//            .readTimeout(60000, TimeUnit.SECONDS)     // 读取超时
//            .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
//            .callTimeout(0, TimeUnit.SECONDS)     // 整体调用超时（可选）
//            .build();
//
//    public void doSSE(CommonParam commonParam,
//                      ProxyParam proxyParam,
//                      HttpServletRequest httpServletRequest,
//                      HttpServletResponse resp) throws Exception {
//
//        String methodName = commonParam.methodName;
//        String targetUrl = ApiHttpProxyCatchUtil.getTargetUrl(methodName);
//        log.info("SSEProxyManager#doSSE start methodName={} targetUrl={}", methodName, targetUrl);
//        if (null == targetUrl) {
//            return;
//        }
//
//        /*
//        ProxyTokenManager tokenManager = SpringBeanUtil.getBean(ProxyTokenManager.class);
//        if (null == tokenManager) {
//            return;
//        }
//        String token = tokenManager.tctToken();
//        */
//
//        String token = proxyParam.getToken();
//        String sessionId = token;
//        log.info("SSEProxyManager#doSSE removeSession token={} sessionId={}", token, sessionId);
//        sessionManager.removeSession(sessionId);
//        //String targetSseUrl = "htttps://push.zhaogangtest.com/pushmsg/scrm/sse/connect?token=57ff1334cd350343cbe06c694d0c07bf&client=pc";
//        String proxyGetParam = proxyParam.getProxyGetParam();
//        String targetSseUrl = targetUrl + "?" + proxyGetParam;
//
//        log.info("SSEProxyManager#doSSE targetSseUrl={} sessionId={}", targetSseUrl, sessionId);
//
//        // startAsync 必须设置
//        AsyncContext asyncContext = httpServletRequest.startAsync();
//        asyncContext.setTimeout(0); // 永不超时，防止连接自动关闭
//
//        resp.setContentType(CONTENT_TYPE_EVENT_STREAM);
//        resp.setCharacterEncoding(CONTENT_CHARSET);
//        resp.setHeader(HEAD_CACHE_CONTROL, HEAD_NO_CACHE);
//        resp.setHeader(HEAD_CONNECTION, HEAD_KEEP_ALIVE);
//
//        PrintWriter writer = resp.getWriter();
//        // 立即返回一段注释内容，触发浏览器的 onopen
//        writer.write(": connected\n\n"); // 注释行，不会被客户端 onmessage 触发
//        writer.flush();
//
//        asyncContext.start(() -> {
//            startSSEProxy(asyncContext, sessionId, targetSseUrl);
//        });
//
//    }
//
//    private void startSSEProxy(AsyncContext asyncContext,
//                               String sessionId,
//                               String targetSseUrl) {
//        EventSourceListener listener = new EventSourceListener() {
//            @Override
//            public void onOpen(EventSource eventSource, okhttp3.Response response) {
//                log.info("SSEProxyManager#onOpen Connected to SSE source.");
//            }
//
//            @Override
//            public void onEvent(EventSource eventSource, String id, String type, String data) {
//                try {
//                    log.info("SSEProxyManager#onEvent SSE Received SSE event: {}", data);
//                    sessionManager.refresh(sessionId);
//                    PrintWriter writer = asyncContext.getResponse().getWriter();
//                    writer.write("data:" + data + "\n\n");
//                    // 必须 flush，数据才会实时推送到客户端
//                    writer.flush();
//                } catch (Exception e) {
//                    log.error("SSEProxyManager#onEvent client connection lost: " + e.getMessage(), e);
//                    sessionManager.removeSession(sessionId); // 清理连接
//                }
//            }
//
//            @Override
//            public void onClosed(EventSource eventSource) {
//                log.info("SSEProxyManager#onClosed SSE connection closed.");
//                sessionManager.removeSession(sessionId);
//            }
//
//            @Override
//            public void onFailure(EventSource eventSource, Throwable t, Response response) {
//                log.error("SSEProxyManager#onFailure connection failed: " + t.getMessage(), t);
//                sessionManager.removeSession(sessionId);
//            }
//        };
//
//        Request request = new Request.Builder()
//                .url(targetSseUrl)
//                .build();
//        log.info("SSEProxyManager#doSSE createFactory targetSseUrl={}", targetSseUrl);
//        // 创建 EventSource 连接
//        EventSource eventSource = EventSources.createFactory(client).newEventSource(request, listener);
//        // 注册 session
//        sessionManager.addSession(sessionId, new SseProxySession(asyncContext, eventSource));
//    }
//}
