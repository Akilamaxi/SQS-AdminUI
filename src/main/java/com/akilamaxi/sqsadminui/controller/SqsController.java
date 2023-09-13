package com.akilamaxi.sqsadminui.controller;

import com.akilamaxi.sqsadminui.service.SqsService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class SqsController {

    @Autowired
    private SqsService sqsService;

    @SneakyThrows
    @GetMapping("/")
    public String index(Model model) {
        List<String> queues = sqsService.listQueues();
        model.addAttribute("queues", queues);
        return "index";
    }

    @PostMapping("/create-queue")
    public String createQueue(@RequestParam String queueName, @RequestParam(required = false) boolean isFifo) {
        sqsService.createQueue(queueName, isFifo);
        return "redirect:/";
    }


    @PostMapping("/send-message")
    public String sendMessage(
            @RequestParam String queueUrl,
            @RequestParam String message,
            @RequestParam(name = "attributeName[]") String[] attributeNames,
            @RequestParam(name = "attributeValue[]") String[] attributeValues,
            @RequestParam(name = "attributeDataType[]") String[] attributeDataTypes,
            @RequestParam(required = false) String deduplicationId,
            @RequestParam(required = false) String groupId) {

        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        for (int i = 0; i < attributeNames.length; i++) {
            messageAttributes.put(attributeNames[i], MessageAttributeValue.builder()
                    .stringValue(attributeValues[i])
                    .dataType(attributeDataTypes[i])
                    .build());
        }

        sqsService.sendMessage(queueUrl, message, messageAttributes, deduplicationId, groupId);
        return "redirect:/";
    }

    @PostMapping("/delete-queue")
    public String deleteQueue(@RequestParam String queueUrl) {
        sqsService.deleteQueue(queueUrl);
        return "redirect:/";
    }

    @GetMapping("/receive-message")
    @ResponseBody
    public List<Message> receiveMessage(@RequestParam String queueUrl) throws ExecutionException, InterruptedException {
        return sqsService.receiveMessage(queueUrl);
    }


}

