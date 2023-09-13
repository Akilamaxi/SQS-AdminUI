package com.akilamaxi.sqsadminui.config;

import com.akilamaxi.sqsadminui.serializer.MessageSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import software.amazon.awssdk.services.sqs.model.Message;

@Configuration
public class AppConfig {

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializerByType(Message.class, new MessageSerializer());
        return builder;
    }

}
