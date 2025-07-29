package com.phegondev.auth2Peoject.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Configuration {

    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
