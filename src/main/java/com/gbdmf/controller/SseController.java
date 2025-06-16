package com.gbdmf.controller;

import com.gbdmf.sse.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SseController {

    @Autowired
    private SseService sseService;

    @GetMapping("/connect/{clientId}")
    public SseEmitter connect(@PathVariable String clientId) {
        return sseService.createConnection(clientId);
    }

    @PostMapping("/send/{clientId}")
    public String sendMessage(@PathVariable String clientId, @RequestBody String message) {
        return sseService.sendMessage(clientId, message);
    }

    @DeleteMapping("/disconnect/{clientId}")
    public void disconnect(@PathVariable String clientId) {
        sseService.removeClient(clientId);
    }
}
