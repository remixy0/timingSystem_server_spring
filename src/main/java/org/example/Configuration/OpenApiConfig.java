package org.example.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BLResult API",
                version = "1.0.0",
                description = "Main documentation of API for IOs and web application.",
                contact = @Contact(
                        name = "Stanisław Sikora",
                        email = "stanislawsikora626@gmail.com"
                )
        )
)

public class OpenApiConfig {
}