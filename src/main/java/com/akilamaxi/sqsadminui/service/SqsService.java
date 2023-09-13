package com.akilamaxi.sqsadminui.service;

import com.akilamaxi.sqsadminui.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.CONTENT_BASED_DEDUPLICATION;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.FIFO_QUEUE;

@Slf4j
@Service
public class SqsService {

    @Autowired
    private SqsAsyncClient sqsClient;

    public String createQueue(String queueName, boolean isFifo) throws ExecutionException, InterruptedException {
        CreateQueueRequest.Builder requestBuilder = CreateQueueRequest.builder();

        if (isFifo) {
            Map<QueueAttributeName, String> attributes = new HashMap<>();
            attributes.put(FIFO_QUEUE, "true");
            attributes.put(CONTENT_BASED_DEDUPLICATION, "true"); // Automatically generates the deduplication ID based on the content, if not provided.
            requestBuilder.attributes(attributes);
            queueName += ".fifo";
        }

        return sqsClient.createQueue(requestBuilder.queueName(queueName).build()).get().queueUrl();
    }

    public void sendMessage(String queueUrl, String message, Map<String, MessageAttributeValue> messageAttributes,
                            String deduplicationId, String groupId) {
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageAttributes(messageAttributes)
                .messageDeduplicationId(deduplicationId)
                .messageGroupId(groupId)
                .build();
        sqsClient.sendMessage(sendMessageRequest);
    }

    public List<String> listQueues() throws ExecutionException, InterruptedException {
        final CompletableFuture<ListQueuesResponse> listQueues = sqsClient.listQueues();
        return listQueues.get().queueUrls();
    }

    public void deleteQueue(String queueUrl) {
        sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build());
    }

    public List<MessageDTO> receiveMessage(String queueUrl) throws ExecutionException, InterruptedException {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)  // You can adjust this number based on your needs
                .messageAttributeNames(Collections.singletonList("All")) // Fetch all message attributes
                .build();

        CompletableFuture<ReceiveMessageResponse> response = sqsClient.receiveMessage(receiveMessageRequest);
        List<Message> messages = response.get().messages();

        List<MessageDTO> messageDTOs = messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(message.messageId());
            dto.setAttributes(message.messageAttributes().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stringValue())));
            dto.setBody(message.body());

            // For SentDate and ReceivedDate, you might need additional processing
            // Here, I'm assuming these are custom attributes. If not, adjust accordingly.
            dto.setSentDate(message.attributes().get("SentTimestamp"));
            dto.setReceivedDate(message.attributes().get("ApproximateFirstReceiveTimestamp"));

            return dto;
        }).collect(Collectors.toList());

        // Process the original messages
        messages.forEach(message -> {
            log.info(message.body());
            // Delete the message after processing.
            //deleteMessage(queueUrl, message.receiptHandle());
        });

        return messageDTOs;
    }


    private void deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();

        sqsClient.deleteMessage(deleteMessageRequest);
    }

}
