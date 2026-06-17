package com.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor  
@AllArgsConstructor
@Data
public class MembershipDTO {
    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private UUID membershipPlanId;
    private LocalDate affiliationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}