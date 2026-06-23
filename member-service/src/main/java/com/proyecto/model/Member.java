package com.proyecto.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "document_number"})
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @JsonProperty("name")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @JsonProperty("lastName")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 50)
    private String plan;

    @JsonProperty("expiresAt")
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    private String email;
    private String phone;

    @Column(name = "document_type", length = 20)
    private String documentType;

    @JsonProperty("dni")
    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(length = 20)
    private String status = "active";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}