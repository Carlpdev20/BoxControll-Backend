package com.proyecto.model;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    private String documentNumber; // El DNI que digitó
    private String memberName;     // Nombre completo para el historial visual
    private LocalDateTime accessTime = LocalDateTime.now();
    private boolean allowed;       // true = Entró, false = Bloqueado
    private String message;        // "Acceso Permitido", "Membresía Vencida", etc.
}