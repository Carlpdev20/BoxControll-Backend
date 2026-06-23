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
    public ResponseEntity<Member> crear(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader, // Recuperamos el Tenant del Gateway
            @RequestBody MemberDTO dto) {
        
        UUID tenantId = UUID.fromString(tenantIdHeader);
        dto.setTenantId(tenantId);
        
        return ResponseEntity.ok(memberService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> actualizar(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader, 
            @PathVariable UUID id, 
            @RequestBody MemberDTO dto) {
        
        UUID tenantId = UUID.fromString(tenantIdHeader); // 🔑 Parseo explícito y seguro
        return ResponseEntity.ok(memberService.actualizar(tenantId, id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Member> eliminar(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader, 
            @PathVariable UUID id) {
        
        UUID tenantId = UUID.fromString(tenantIdHeader);
        // 🔄 Ejecutamos el conmutador y devolvemos el Member actualizado con su nuevo status
        Member memberActualizado = memberService.conmutarEstado(tenantId, id); 
        return ResponseEntity.ok(memberActualizado);
    }
}