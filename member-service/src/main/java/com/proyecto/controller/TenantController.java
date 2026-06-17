package com.proyecto.controller;

import com.proyecto.dto.TenantDTO;
import com.proyecto.model.Tenant;
import com.proyecto.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public ResponseEntity<List<Tenant>> listar() {
        return ResponseEntity.ok(tenantService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Tenant> crear(@RequestBody TenantDTO dto) {
        return ResponseEntity.ok(tenantService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> actualizar(@PathVariable UUID id, @RequestBody TenantDTO dto) {
        return ResponseEntity.ok(tenantService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        tenantService.desactivarTenant(id); // Ejecuta la suspensión del gimnasio sin borrar sus datos históricos
        return ResponseEntity.noContent().build();
    }
}