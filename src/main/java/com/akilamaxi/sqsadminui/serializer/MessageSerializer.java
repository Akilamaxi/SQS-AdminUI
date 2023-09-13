package com.akilamaxi.sqsadminui.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.IOException;

public class MessageSerializer extends JsonSerializer<Message> {

    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("messageId", value.messageId());
        gen.writeStringField("body", value.body());
        // ... any other fields you want ...
        gen.writeEndObject();
    }
}
