package com.zendesk.zcc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:zendeskapi.properties")
@Getter
public class ZendeskPropertiesReader {

    @Value("${base-url}")
    private String baseUrl;

    @Value("${ticket}")
    private String ticket;

    @Value("${username}")
    private String username;

    @Value("${api-token}")
    private String token;

}
