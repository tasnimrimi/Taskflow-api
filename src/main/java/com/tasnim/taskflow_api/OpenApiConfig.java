package com.tasnim.taskflow_api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow API")
                        .description(
                                "A task-management REST API built with Java, Spring Boot, and PostgreSQL."
                        )
                        .version("1.0.0"));
    }
}