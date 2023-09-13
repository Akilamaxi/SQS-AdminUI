package com.akilamaxi.sqsadminui.controller;

import com.akilamaxi.sqsadminui.dto.MessageDTO;
import com.akilamaxi.sqsadminui.service.SqsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.ExecutionException;
@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    SqsService sqsService;

    @MessageMapping("/receiveMessage")
    public void receiveMessage(String queueUrl) throws ExecutionException, InterruptedException {
        // This is an example. In reality, you might want to run a separate thread or a scheduled job that
        // listens for SQS messages continuously and sends them over the WebSocket.
        List<MessageDTO> messages = sqsService.receiveMessage(queueUrl);
        log.info("receiveMessage started ");
        for (MessageDTO message : messages) {
            log.info("receiveMessage reading message {}", message);
            template.convertAndSend("/queue/received", message);
        }
    }
}
