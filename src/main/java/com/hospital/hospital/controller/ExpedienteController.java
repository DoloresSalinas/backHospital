package com.hospital.hospital.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hospital.hospital.model.entity.Expediente;
import com.hospital.hospital.service.ExpedienteService;
import com.hospital.hospital.service.PacienteService;

@RestController
@RequestMapping("/expedientes")
@CrossOrigin(origins = "*")
public class ExpedienteController {

    private final ExpedienteService expedienteService;
    private final PacienteService pacienteService;

    public ExpedienteController(ExpedienteService expedienteService, PacienteService pacienteService) {
        this.expedienteService = expedienteService;
        this.pacienteService = pacienteService;
    }

    // método solo se usará para ERRORES o mensajes específicos
    private ResponseEntity<Object> generarRespuesta(Object data, String mensaje, HttpStatus status) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", status.value());
        respuesta.put("mensaje", mensaje);
        respuesta.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        if (data != null) {
            respuesta.put("data", data);
        }
        return new ResponseEntity<>(respuesta, status);
    }

    @PostMapping
    public ResponseEntity<Object> createExpediente(@RequestBody Expediente expediente) {
        try {

            Expediente nuevo = expedienteService.saveExpediente(expediente);

            return generarRespuesta(nuevo, "Expediente creado exitosamente", HttpStatus.CREATED);

        } catch (Exception e) {
            return generarRespuesta(null,
                    "Error al crear el expediente: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllExpedientes() {
        List<Expediente> lista = expedienteService.getAllExpedientes(); 
        return generarRespuesta(lista, "Lista de expedientes obtenida exitosamente", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getExpedienteById(@PathVariable Long id) {
        Expediente e = expedienteService.getExpedienteById(id);
        if (e != null) {
            // ÉXITO: Envía solo los datos del expediente
            return generarRespuesta(e, "Expediente encontrado", HttpStatus.OK);
        }
        // ERROR: Envía mensaje y estatus
        return generarRespuesta(null,"El expediente con ID " + id + " no existe", HttpStatus.NOT_FOUND);
    } 

    // @GetMapping("/paciente/{idPaciente}")
    // public ResponseEntity<Object> getExpedientesByPaciente(@PathVariable Integer idPaciente) { 
    //     List<Expediente> e = expedienteService.getExpedientesByPaciente(idPaciente);
        
    //     if (e != null && !e.isEmpty()) {
    //         return generarRespuesta(e, "Expedientes encontrados para el paciente " + idPaciente, HttpStatus.OK);
    //     }
        
    //     return generarRespuesta(null, "El paciente con ID " + idPaciente + " no tiene expedientes asociados", HttpStatus.NOT_FOUND);
    // } 

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<Object> getExpedienteByPaciente(@PathVariable Integer idPaciente) { 
        
        Expediente e = expedienteService.getExpedienteActivo(idPaciente);

        if (e != null) {
            return generarRespuesta(e, "Expediente actual del paciente " + idPaciente, HttpStatus.OK);
        }

        return generarRespuesta(null, "No hay expediente activo para el paciente " + idPaciente, HttpStatus.NOT_FOUND);
    } 

    // @PatchMapping("/expediente-update/{idExpediente}")
    // public ResponseEntity<?> actualizarExpediente(
    //         @PathVariable Long idExpediente,
    //         @RequestBody Map<String, Object> cambios) {

    //     Expediente nuevo = expedienteService.actualizarExpediente(idExpediente, cambios);

    //     return ResponseEntity.ok(nuevo);
    // }

    @PatchMapping("/expediente-update/{idExpediente}")
    public ResponseEntity<Object> actualizarExpediente(
            @PathVariable Long idExpediente,
            @RequestBody Map<String, Object> cambios) {
        try {
            Expediente nuevo = expedienteService.actualizarExpediente(idExpediente, cambios);
            return generarRespuesta(nuevo, "Expediente actualizado correctamente", HttpStatus.OK);
        } catch (RuntimeException e) { 
            return generarRespuesta(null, e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(); // o log.error
            return generarRespuesta(null, "Error interno al actualizar el expediente", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @RequestBody Expediente actualizado) {
        Expediente updated = expedienteService.updateExpediente(id, actualizado);
        if (updated != null) {
            // ÉXITO: Envía solo los datos actualizados
            return generarRespuesta(updated, "Expediente actualizado exitosamente", HttpStatus.OK);
        }
        // ERROR: Envía mensaje y estatus
        return generarRespuesta(null, "No se pudo actualizar: ID " + id + " no encontrado", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteExpediente(@PathVariable Long id) {
        Expediente e = expedienteService.getExpedienteById(id);
        if (e != null) {
            expedienteService.deleteExpediente(id);
            // ÉXITO: En un delete exitoso, solemos enviar un mensaje de confirmación
            return generarRespuesta(null, "Expediente eliminado correctamente", HttpStatus.OK);
        }
        // ERROR: Envía mensaje y estatus
        return generarRespuesta(null, "Error al eliminar: ID " + id + " no existe", HttpStatus.NOT_FOUND);
    }
}