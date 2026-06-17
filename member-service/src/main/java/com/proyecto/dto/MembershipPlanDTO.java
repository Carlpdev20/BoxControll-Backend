package com.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor  
@AllArgsConstructor
@Data
public class MembershipPlanDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private Integer durationDays;
    private BigDecimal price;
    private String currency;
    private Boolean active;
}