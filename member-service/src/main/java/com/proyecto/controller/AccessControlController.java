package com.proyecto.controller;

import com.proyecto.model.Attendance;
import com.proyecto.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/access-control")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AccessControlController {

    private final AccessControlService accessControlService;

    @PostMapping("/check-in")
    public ResponseEntity<Attendance> checkIn(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @RequestParam String dni) { // Recibe el DNI como Query Param (?dni=12345678)
        
        UUID tenantId = UUID.fromString(tenantIdHeader);
        return ResponseEntity.ok(accessControlService.procesarCheckIn(tenantId, dni));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Attendance>> obtenerHistorial(@RequestHeader("X-Tenant-Id") String tenantIdHeader) {
        UUID tenantId = UUID.fromString(tenantIdHeader);
        return ResponseEntity.ok(accessControlService.listarHistorial(tenantId));
    }
}