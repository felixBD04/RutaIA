package com.rutaia.backend.integration.n8n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Cliente HTTP que llama al webhook del flujo RAG en n8n.
 *
 * Los timeouts son importantes: sin ellos, si n8n u OpenRouter se quedan
 * colgados, la peticion del estudiante esperaria para siempre.
 */
@Slf4j
@Component
public class N8nClient {

    private final RestClient restClient;
    private final String webhookUrl;

    public N8nClient(@Value("${rutaia.n8n.webhook-url}") String webhookUrl,
                     @Value("${rutaia.n8n.timeout-segundos:60}") int timeoutSegundos) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));             // tiempo para conectar con n8n
        factory.setReadTimeout(Duration.ofSeconds(timeoutSegundos));  // tiempo para que responda

        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.webhookUrl = webhookUrl;
    }

    /**
     * Envia la consulta a n8n y devuelve su respuesta.
     *
     * @throws RestClientException si n8n no responde, responde con un codigo de error
     *                             (4xx/5xx) o devuelve un cuerpo que no se puede leer.
     */
    public N8nRespuesta consultar(N8nConsultaRequest request) {
        log.info("Enviando consulta {} a n8n", request.consultaId());
        return restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(N8nRespuesta.class);
    }
}
