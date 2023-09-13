package com.akilamaxi.sqsadminui.controller;

import com.akilamaxi.sqsadminui.dto.MessageDTO;
import com.akilamaxi.sqsadminui.dto.QueueRequest;
import com.akilamaxi.sqsadminui.dto.ReceiveMessageRequest;
import com.akilamaxi.sqsadminui.dto.SendMessageRequest;
import com.akilamaxi.sqsadminui.service.SqsService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController  // Changed to @RestController for JSON responses
@RequestMapping("/sqs")  // Added a base URL to organize endpoints
public class SqsAsyncController {

    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    @Autowired
    private SqsService sqsService;

    @SneakyThrows
    @GetMapping("/list")
    public ResponseEntity<List<String>> listQueues() {
        List<String> queues = sqsService.listQueues();
        return ResponseEntity.ok(queues);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createQueue(@RequestBody QueueRequest request) {
        Map<String, Object> response = new HashMap<>();
        String queueUrl;
        try {
            queueUrl = sqsService.createQueue(request.getQueueName(), request.getIsFifo());
            response.put("queueUrl", queueUrl);
            response.put(SUCCESS, true);
        } catch(Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
    @PostMapping("/send-message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody SendMessageRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            for (SendMessageRequest.MessageAttribute attr : request.getAttributes()) {
                messageAttributes.put(attr.getName(), MessageAttributeValue.builder()
                        .stringValue(attr.getValue())
                        .dataType(attr.getDataType())
                        .build());
            }

            sqsService.sendMessage(request.getQueueUrl(), request.getMessage(), messageAttributes, request.getDeduplicationId(), request.getGroupId());
            response.put(SUCCESS, true);
        } catch(Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/delete-queue")
    public ResponseEntity<Map<String, Object>> deleteQueue(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String queueUrl = payload.get("queueUrl");
            sqsService.deleteQueue(queueUrl);
            response.put(SUCCESS, true);
        } catch(Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/receive-message")
    public ResponseEntity<List<MessageDTO>> receiveMessage(@RequestBody ReceiveMessageRequest request) {
        try {
            List<MessageDTO> messages = sqsService.receiveMessage(request.getQueueUrl());

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);  // Consider returning an error message
        }
    }
}
