package com.proyecto.controller;

import com.proyecto.dto.MembershipPlanDTO;
import com.proyecto.model.MembershipPlan;
import com.proyecto.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService planService;

    @GetMapping
    public ResponseEntity<List<MembershipPlan>> listarPorTenant(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ResponseEntity.ok(planService.listarPorTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipPlan> buscar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(planService.buscarPorId(tenantId, id));
    }

    @PostMapping
    public ResponseEntity<MembershipPlan> crear(@RequestBody MembershipPlanDTO dto) {
        return ResponseEntity.ok(planService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembershipPlan> actualizar(
            @RequestHeader("X-Tenant-Id") UUID tenantId, 
            @PathVariable UUID id, 
            @RequestBody MembershipPlanDTO dto) {
        return ResponseEntity.ok(planService.actualizar(tenantId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        planService.desactivarPlan(tenantId, id); // Redirigido a la desactivación del plan comercial
        return ResponseEntity.noContent().build();
    }
}