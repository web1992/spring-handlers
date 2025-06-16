package com.gbdmf.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SseClientExample {

    private static final String UPSTREAM_SSE_URL = "https://push.zhaogangtest.com/pushmsg/scrm/sse/connect?token=57ff1334cd350343cbe06c694d0c07bf&client=pc";


    static String sendUrl = "https://testgateway.pangmaoq.com/safeapi/inner/aiAssistant/sendAiChatMsg";


    public static void main(String[] args) throws InterruptedException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(UPSTREAM_SSE_URL)  // 替换为你的 SSE 地址
                .header("Accept", "text/event-stream")
                .build();

        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                System.out.println("Connected to SSE server");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                System.out.println("Received event: " + data);
            }

            @Override
            public void onClosed(EventSource eventSource) {
                System.out.println("SSE connection closed");
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                System.err.println("SSE connection error: " + t.getMessage());
            }
        };

        EventSource.Factory factory = EventSources.createFactory(client);
        factory.newEventSource(request, listener);


        // 顶层 Map
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("chatFrom", "single");
        jsonMap.put("chatSessionId", UUID.fastUUID().toString());
        jsonMap.put("aiModelType", "DS_V3");
        jsonMap.put("netSearch", 0);

        // contents 列表
        Map<String, Object> contentItem = new HashMap<>();
        contentItem.put("content", "hello\n");
        contentItem.put("contentType", "text");

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(contentItem);

        jsonMap.put("contents", contents);

        // 转成 JSON 字符串
        String jsonOutput = JSON.toJSONString(jsonMap);
        System.out.println(jsonOutput);

//        TimeUnit.SECONDS.sleep(10);
        String uuid = UUID.fastUUID().toString();

        HttpResponse execute = HttpUtil.createPost(sendUrl)
                .header("AppId", "channel_pmys_zg_2023072101")
                .header("isaio", "1")
                .header("token", "57ff1334cd350343cbe06c694d0c07bf")
                .header(" Content-Type", "application/json")
                .body(jsonOutput).execute();

        System.out.println("post >>>>>>>>>>>>>>>>>>>");
        System.out.println(execute.body());
    }
}
