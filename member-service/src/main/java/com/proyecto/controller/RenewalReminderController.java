package com.proyecto.controller;

import com.proyecto.dto.RenewalReminderDTO;
import com.proyecto.model.RenewalReminder;
import com.proyecto.service.RenewalReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RenewalReminderController {

    private final RenewalReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<RenewalReminder>> listarPorTenant(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ResponseEntity.ok(reminderService.listarPorTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RenewalReminder> buscar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(reminderService.buscarPorId(tenantId, id));
    }

    @PostMapping
    public ResponseEntity<RenewalReminder> crear(@RequestBody RenewalReminderDTO dto) {
        return ResponseEntity.ok(reminderService.crear(dto));
    }

    @PutMapping("/{id}/sent")
    public ResponseEntity<RenewalReminder> marcarEnviado(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(reminderService.marcarEnviado(tenantId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        reminderService.eliminarRecordatorio(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}