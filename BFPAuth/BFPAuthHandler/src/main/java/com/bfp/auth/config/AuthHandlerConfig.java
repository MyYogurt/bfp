package com.bfp.auth.config;

import com.bfp.auth.AuthHandler;
import com.bfp.auth.AuthHandlerDefault;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthHandlerConfig {
    @Bean
    public AuthHandler authHandler() {
        return new AuthHandlerDefault();
    }
}
