package com.hospital.hospital.controller;
import com.hospital.hospital.model.dto.MedicoDTO;
import com.hospital.hospital.model.entity.Medico;
import com.hospital.hospital.service.MedicoService;
import com.hospital.hospital.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/medico")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrarMedico(@RequestBody Map<String, Object> body) {
        try {
            Medico medico = medicoService.registrarMedico(
                (String) body.get("correo"),
                (String) body.get("contrasena"),
                (String) body.get("nombre"),
                (String) body.get("apPaterno"),
                (String) body.get("apMaterno"),
                (String) body.get("especialidad"),
                (String) body.get("cedulaProfesional"),
                (String) body.get("numEmpleado"),
                Medico.Turno.valueOf((String) body.get("turno")),
                (String) body.get("consultorio"),
                ((Integer) body.get("idUm")).shortValue()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(medico);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Ver MI perfil — requiere token
    @GetMapping("/mi-perfil")
    public ResponseEntity<?> getMiPerfil() {
        try {
            Integer idUsuario = JwtUtil.getIdUsuario();
            return ResponseEntity.ok(medicoService.getMiPerfil(idUsuario));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Editar MI perfil — requiere token
    @PutMapping("/mi-perfil")
    public ResponseEntity<?> editarPerfil(@RequestBody MedicoDTO dto) {
        try {
            Integer idUsuario = JwtUtil.getIdUsuario();
            return ResponseEntity.ok(medicoService.editarPerfil(idUsuario, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Borrado lógico — requiere token
    @DeleteMapping("/mi-perfil")
    public ResponseEntity<?> desactivarMedico() {
        try {
            Integer idUsuario = JwtUtil.getIdUsuario();
            medicoService.desactivarMedico(idUsuario);
            return ResponseEntity.ok(Map.of("mensaje", "Cuenta desactivada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/menos-citas")
    public ResponseEntity<?> getMedicoConMenosCitas() {
    try {
        Medico medico = medicoService.getMedicoConMenosCitas();
        return ResponseEntity.ok(medico);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }
}
}
