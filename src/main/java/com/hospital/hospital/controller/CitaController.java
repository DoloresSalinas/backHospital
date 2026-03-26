package com.hospital.hospital.controller;

import com.hospital.hospital.model.dto.CitaDTO;
import com.hospital.hospital.model.dto.CompletarCitaRequest;
import com.hospital.hospital.model.entity.Cita;
import com.hospital.hospital.service.CitaService;
import com.hospital.hospital.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cita")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping("/agendar")
    public ResponseEntity<?> agendarCita(@RequestBody Map<String, Object> body) {
        try {
            Integer idPaciente = JwtUtil.getIdUsuario(); // ← del token
            Cita cita = citaService.agendarCita(
                    idPaciente,
                    LocalDate.parse((String) body.get("fecha")),
                    LocalTime.parse((String) body.get("hora")),
                    (String) body.get("motivo"),
                    Cita.TipoCita.valueOf((String) body.get("tipo")));
            return ResponseEntity.status(HttpStatus.CREATED).body(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<?> obtenerCitas(@PathVariable Integer idPaciente) {
        try {
            List<Cita> citas = citaService.obtenerCitasPorPaciente(idPaciente);
            return ResponseEntity.ok(citas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint para que el paciente vea sus citas (autenticado)
    @GetMapping("/mis-citas")
    public ResponseEntity<?> getMisCitasPaciente() {
        try {
            Integer idUsuario = JwtUtil.getIdUsuario();
            // ✅ Usa el nuevo método que resuelve idPaciente internamente
            List<Cita> citas = citaService.obtenerCitasPorIdUsuario(idUsuario);
            return ResponseEntity.ok(citas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/cancelar/{idCita}")
    public ResponseEntity<?> cancelarCita(@PathVariable Integer idCita) {
        try {
            return ResponseEntity.ok(citaService.cancelarCita(idCita));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/citas-medico")
    public ResponseEntity<?> getMisCitas() {
        try {
            Integer idUsuario = JwtUtil.getIdUsuario();
            List<CitaDTO> citas = citaService.getCitasByMedico(idUsuario);
            return ResponseEntity.ok(citas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<Void> completarCita(
            @PathVariable Integer id,
            @RequestBody CompletarCitaRequest request) {
        citaService.completarCita(id, request);
        return ResponseEntity.ok().build();
    }
}