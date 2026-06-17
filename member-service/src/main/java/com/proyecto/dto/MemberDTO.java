package com.proyecto.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor  
@AllArgsConstructor
@Data
public class MemberDTO {
    private UUID id;
    private UUID tenantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String documentType;
    private String documentNumber;
    private String status;
}