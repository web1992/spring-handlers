package com.gbdmf.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
public class SseProxyController {

    private static final String UPSTREAM_SSE_URL = "https://tcttest.pangmaoq.com/frontsu/qiwei/ai?token=57ff1334cd350343cbe06c694d0c07bf&chatFrom=null";

    // {"chatFrom":"single","chatSessionId":"c4dfb7e9-499c-4df4-b2b7-5c62c64245a8","aiModelType":"DS_V3","netSearch":0,"contents":[{"content":"hello\n","contentType":"text"}]}

    String msgBody = "{\"chatFrom\":\"single\",\"chatSessionId\":\"%s\",\"aiModelType\":\"DS_V3\",\"netSearch\":0,\"contents\":[{\"content\":\"hello\\n\",\"contentType\":\"text\"}]}";

    String sendUrl = "https://testgateway.pangmaoq.com/inner/aiAssistant/sendAiChatMsg";


    @GetMapping("/sse-proxy")
    public SseEmitter proxySse() {
        SseEmitter emitter = new SseEmitter(0L); // 无限超时

        String uuid = UUID.fastUUID().toString();
        String body = String.format(msgBody, uuid);
//        String post = HttpUtil.post(sendUrl, String.format(msgBody, uuid));

        HttpResponse execute = HttpUtil.createPost(sendUrl)
                .header("token", "57ff1334cd350343cbe06c694d0c07bf")
                .body(body).execute();
        System.out.println("post >>>>>>>>>>>>>>>>>>>");
        System.out.println(execute.body());

        new Thread(() -> {
            HttpResponse connection = null;
            InputStream inputStream = null;
            try {
                connection = HttpUtil.createGet(UPSTREAM_SSE_URL)
                        .header("Accept", "text/event-stream")
                        .setConnectionTimeout(5000)
                        .setReadTimeout(0) // 不设置读取超时（SSE长链接）
                        .execute();

                inputStream = connection.bodyStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // SSE 数据通常是以 data: 开头
                    if (line.startsWith("data:")) {
                        String data = line.substring("data:".length()).trim();
                        emitter.send(data);
                    }
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                IoUtil.close(connection);
            }
        }).start();

        return emitter;
    }
}
