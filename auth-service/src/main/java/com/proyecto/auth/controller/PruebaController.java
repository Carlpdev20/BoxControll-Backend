package com.proyecto.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prueba")
public class PruebaController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndPoint() {
        return ResponseEntity.ok("Este es un Endpoint público de BoxControll");
    }

    @GetMapping("/private")
    public ResponseEntity<String> privateEndPoint() {
        return ResponseEntity.ok("Este es un endpoint privado. ¡Autenticado en BoxControll con éxito!");
    }
}