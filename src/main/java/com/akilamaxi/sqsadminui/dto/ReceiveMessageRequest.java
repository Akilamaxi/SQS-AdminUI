package com.akilamaxi.sqsadminui.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReceiveMessageRequest {
    private String queueUrl;
}
