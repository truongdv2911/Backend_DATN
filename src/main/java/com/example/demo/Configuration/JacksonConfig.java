package com.example.demo.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer strictDeserialization() {
//        return builder -> builder
//                .featuresToEnable(DeserializationFeature.FAIL);
//    }
}
