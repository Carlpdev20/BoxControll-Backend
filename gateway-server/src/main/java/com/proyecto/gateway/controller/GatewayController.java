package com.proyecto.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod; // 👈 Agregado
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
// 🔄 OPTIMIZADO: Declaramos explícitamente los métodos permitidos para el Preflight de CORS
@CrossOrigin(
    origins = "http://localhost:4200", 
    allowCredentials = "true", 
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${jwt.secret}")
    private String secretString;

    @RequestMapping("/auth/**")
    public ResponseEntity<String> proxyAuthService(
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request) {
        return ejecutarProxySinJwt("http://localhost:8081", body, method, request);
    }

    @RequestMapping("/api/billing/**")
    public ResponseEntity<String> proxyBillingService(
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request) {
        return ejecutarProxyConJwt("http://localhost:8082", body, method, request);
    }

    @RequestMapping("/api/**")
    public ResponseEntity<String> proxyMemberService(
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request) {
        return ejecutarProxyConJwt("http://localhost:8084", body, method, request);
    }

    // ============================================================================
    // MÉTODOS UTILITARIOS OPTIMIZADOS
    // ============================================================================
    
    private ResponseEntity<String> ejecutarProxyConJwt(String baseServiceUrl, String body, HttpMethod method, HttpServletRequest request) {
        // 🛡️ ESCUDO CRÍTICO: Si es un OPTIONS, respondemos 200 OK de inmediato para aprobar el CORS
        if (method == HttpMethod.OPTIONS) {
            return ResponseEntity.ok().build();
        }

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> {
                    if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                        headers.add(headerName, request.getHeader(headerName));
                    }
                });

        // 🔒 --- VALIDA_JWT (Solo se ejecuta para GET, POST, PUT, DELETE) ---
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
                
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                String tenantId = claims.get("tenant_id", String.class);
                
                if (tenantId != null) {
                    headers.set("X-Tenant-Id", tenantId); 
                } else {
                    return ResponseEntity.status(401).body("El token no contains un Tenant ID valido.");
                }
                
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Token JWT invalido, alterado o expirado.");
            }
        } else {
            return ResponseEntity.status(401).body("Acceso denegado: Se requiere un Bearer Token de autorizacion.");
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        String targetUrl = baseServiceUrl + request.getRequestURI();

        try {
            return restTemplate.exchange(targetUrl, method, httpEntity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }

    private ResponseEntity<String> ejecutarProxySinJwt(String baseServiceUrl, String body, HttpMethod method, HttpServletRequest request) {
        // 🛡️ ESCUDO CRÍTICO: También para las rutas libres
        if (method == HttpMethod.OPTIONS) {
            return ResponseEntity.ok().build();
        }

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> {
                    if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                        headers.add(headerName, request.getHeader(headerName));
                    }
                });

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        String targetUrl = baseServiceUrl + request.getRequestURI();

        try {
            return restTemplate.exchange(targetUrl, method, httpEntity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }
}