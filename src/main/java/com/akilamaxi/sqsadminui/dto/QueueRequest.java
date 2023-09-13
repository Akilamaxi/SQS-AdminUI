package com.akilamaxi.sqsadminui.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class QueueRequest {
    private String queueName;
    private Boolean isFifo;
}
