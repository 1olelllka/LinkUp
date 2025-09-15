package com.olelllka.gateway.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI defineOpenAPI() {
        Contact contact = new Contact();
        contact.email("olehit32@gmail.com");
        contact.name("Oleh Sichko");

        Info info = new Info()
                .title("Gateway API")
                .version("0.98")
                .description("This API documentation shows fallback endpoint for Gateway service. All of the endpoints for services can be found in 'Select Definition' Tab")
                .contact(contact);

        return new OpenAPI().info(info);
    }
}