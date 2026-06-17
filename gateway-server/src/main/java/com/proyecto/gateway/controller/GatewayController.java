package com.proyecto.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", allowedHeaders = "*")
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping("/auth/**")
    public ResponseEntity<String> proxyAuthService(
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request) {

        String targetUrl = "http://localhost:8081" + request.getRequestURI();

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> {
                    // CRUCIAL: Ignoramos 'host' y 'content-length' para evitar el bloqueo 403
                    if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                        headers.add(headerName, request.getHeader(headerName));
                    }
                });

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(targetUrl, method, httpEntity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }
}