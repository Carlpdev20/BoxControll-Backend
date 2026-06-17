package com.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor  
@AllArgsConstructor
@Data
public class TenantDTO {
    private UUID id;
    private String name;
    private String slug;
    private String email;
    private String phone;
    private Boolean active;
}