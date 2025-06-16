package com.gbdmf.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SseClient {

    private static final String UPSTREAM_SSE_URL = "https://tcttest.pangmaoq.com/frontsu/qiwei/ai?token=57ff1334cd350343cbe06c694d0c07bf&chatFrom=null";

    static String msgBody = "";

    static String sendUrl = "https://testgateway.pangmaoq.com/inner/aiAssistant/sendAiChatMsg";

    public static void main(String[] args) throws InterruptedException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(UPSTREAM_SSE_URL)  // 你的 SSE 接口
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("连接失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("接收到: " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


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

        TimeUnit.SECONDS.sleep(10);
        String uuid = UUID.fastUUID().toString();

        String body = String.format(msgBody, uuid);
        HttpResponse execute = HttpUtil.createPost(sendUrl)
                .header("token", "57ff1334cd350343cbe06c694d0c07bf")
                .body(msgBody).execute();

        System.out.println("post >>>>>>>>>>>>>>>>>>>");
        System.out.println(execute.body());

    }
}
