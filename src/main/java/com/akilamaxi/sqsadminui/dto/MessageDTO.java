package com.akilamaxi.sqsadminui.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MessageDTO {
    private String messageId;
    private Map<String, String> attributes;
    private String sentDate;
    private String receivedDate;
    private String body;
}
