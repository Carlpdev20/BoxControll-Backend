package com.proyecto.controller;

import com.proyecto.dto.MembershipDTO;
import com.proyecto.model.Membership;
import com.proyecto.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memberships")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    public ResponseEntity<List<Membership>> listarPorTenant(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ResponseEntity.ok(membershipService.listarPorTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Membership> buscar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(membershipService.buscarPorId(tenantId, id));
    }

    @PostMapping
    public ResponseEntity<Membership> crear(@RequestBody MembershipDTO dto) {
        return ResponseEntity.ok(membershipService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Membership> actualizar(
            @RequestHeader("X-Tenant-Id") UUID tenantId, 
            @PathVariable UUID id, 
            @RequestBody MembershipDTO dto) {
        return ResponseEntity.ok(membershipService.actualizar(tenantId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        membershipService.cancelarMembresia(tenantId, id); // Redirigido al método de cancelación lógica
        return ResponseEntity.noContent().build();
    }
}