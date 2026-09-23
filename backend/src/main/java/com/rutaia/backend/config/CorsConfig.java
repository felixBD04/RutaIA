package com.rutaia.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: autoriza al frontend (que corre en otro puerto, por ejemplo localhost:3000)
 * a llamar a la API en localhost:8080.
 *
 * Sin esto, el NAVEGADOR bloquea las peticiones por seguridad, aunque desde
 * Swagger o Postman todo funcione (ellos no aplican la politica de CORS).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
