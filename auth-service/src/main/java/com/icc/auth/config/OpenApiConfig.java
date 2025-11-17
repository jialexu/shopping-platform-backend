package com.icc.auth.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description("JWT Authentication and Authorization Service")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chuwa Team")
                                .email("support@chuwa.com")))
                .servers(List.of(
                        new Server().url("http://localhost:9000").description("Local server"),
                        new Server().url("http://auth-service:9000").description("Docker server")
                ));
    }
}
