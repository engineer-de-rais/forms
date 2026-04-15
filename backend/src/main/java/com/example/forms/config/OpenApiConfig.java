package com.example.forms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI formsOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Forms API")
                .description("REST API for Forms MVP backend")
                .version("v1")
                .contact(new Contact().name("Forms Team").email("team@example.com"))
                .license(new License().name("Proprietary")));
    }
}
