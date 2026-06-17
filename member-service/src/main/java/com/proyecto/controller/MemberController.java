package com.proyecto.controller;

import com.proyecto.dto.MemberDTO;
import com.proyecto.model.Member;
import com.proyecto.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<Member>> listarPorTenant(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ResponseEntity.ok(memberService.listarPorTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> buscar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(memberService.buscarPorId(tenantId, id));
    }

    @PostMapping
    public ResponseEntity<Member> crear(@RequestBody MemberDTO dto) {
        return ResponseEntity.ok(memberService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> actualizar(
            @RequestHeader("X-Tenant-Id") UUID tenantId, 
            @PathVariable UUID id, 
            @RequestBody MemberDTO dto) {
        return ResponseEntity.ok(memberService.actualizar(tenantId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader("X-Tenant-Id") UUID tenantId, @PathVariable UUID id) {
        memberService.eliminarLogico(tenantId, id); // Redirigido a la baja lógica segura
        return ResponseEntity.noContent().build();
    }
}