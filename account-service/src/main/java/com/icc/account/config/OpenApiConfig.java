package com.icc.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .description("User account management service for Chuwa Shopping Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chuwa Team")
                                .email("support@chuwa.com")));
    }
}
