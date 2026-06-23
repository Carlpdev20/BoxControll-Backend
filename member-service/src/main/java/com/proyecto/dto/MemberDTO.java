package com.proyecto.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proyecto.model.PlanType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor  
@AllArgsConstructor
@Data
public class MemberDTO {
    private UUID id;
    private UUID tenantId;
    @JsonProperty("name")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    private String email;
    private String phone;
    private String plan;
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    private String documentType;
    @JsonProperty("dni")
    private String documentNumber;
    private String status;
}	