package com.akilamaxi.sqsadminui.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SendMessageRequest {

    private String queueUrl;
    private String message;
    private String deduplicationId;
    private String groupId;
    private List<MessageAttribute> attributes;

    @Data
    @NoArgsConstructor
    public static class MessageAttribute {
        private String name;
        private String value;
        private String dataType;
    }
}

