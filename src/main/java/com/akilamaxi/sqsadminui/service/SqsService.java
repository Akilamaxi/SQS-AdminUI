package com.akilamaxi.sqsadminui.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.CONTENT_BASED_DEDUPLICATION;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.FIFO_QUEUE;

@Service
public class SqsService {

    @Autowired
    private SqsAsyncClient sqsClient;

    public void createQueue(String queueName, boolean isFifo) {
        CreateQueueRequest.Builder requestBuilder = CreateQueueRequest.builder().queueName(queueName);

        if (isFifo) {
            Map<QueueAttributeName, String> attributes = new HashMap<>();
            attributes.put(FIFO_QUEUE, "true");
            attributes.put(CONTENT_BASED_DEDUPLICATION, "true"); // Automatically generates the deduplication ID based on the content, if not provided.
            requestBuilder.attributes(attributes);

            if (!queueName.endsWith(".fifo")) {
                queueName += ".fifo";  // FIFO queue names must end with `.fifo`
            }
        }

        sqsClient.createQueue(requestBuilder.build());
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

    public List<Message> receiveMessage(String queueUrl) throws ExecutionException, InterruptedException {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)  // You can adjust this number based on your needs
                .build();

        CompletableFuture<ReceiveMessageResponse> response = sqsClient.receiveMessage(receiveMessageRequest);

        return response.get().messages();
    }


}
