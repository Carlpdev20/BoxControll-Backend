package com.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor  
@AllArgsConstructor
@Data
public class RenewalReminderDTO {
    private UUID id;
    private UUID tenantId;
    private UUID membershipId;
    private UUID memberId;
    private LocalDate reminderDate;
    private Boolean sent;
    private String contactMethod;
}