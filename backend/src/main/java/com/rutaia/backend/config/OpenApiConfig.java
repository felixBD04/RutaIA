package com.rutaia.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Informacion general que se muestra en la parte superior de Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rutaIaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("RutaIA API")
                .version("1.0.0")
                .description("API REST para recomendar cursos mediante búsqueda semántica y RAG"));
    }
}
