package com.hospital.hospital.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.hospital.model.entity.Expediente;
import com.hospital.hospital.model.entity.Paciente;
import com.hospital.hospital.model.repository.ExpedienteRepository;
import com.hospital.hospital.model.repository.PacienteRepository;
import com.hospital.hospital.model.repository.MedicoRepository;
import com.hospital.hospital.util.JwtUtil;

@Service
public class ExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;  // Se mantiene por si se usa en el futuro

    public ExpedienteService(ExpedienteRepository expedienteRepository,
                             PacienteRepository pacienteRepository,
                             MedicoRepository medicoRepository) {
        this.expedienteRepository = expedienteRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    // Guardar expediente (primera creación)
    public Expediente saveExpediente(Expediente expediente) {
        Integer idUsuario = JwtUtil.getIdUsuario();

        // Buscar paciente por id_usuario
        Paciente paciente = pacienteRepository
                .obtenerConUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        expediente.setIdPaciente(paciente);

        // Si el expediente ya tiene médico asignado (desde el frontend), se deja.
        // Si no, se podría intentar obtener el médico del usuario actual.
        // Por ahora, si el médico es nulo, lanzamos excepción.
        if (expediente.getMedico() == null) {
            throw new RuntimeException("El expediente debe tener un médico asignado");
        }

        return expedienteRepository.save(expediente);
    }

    public List<Expediente> getAllExpedientes() {
        return expedienteRepository.findAll();
    }

    public List<Expediente> getExpedientesByPaciente(Integer idPaciente) {
        return expedienteRepository.findByIdPaciente_IdPaciente(idPaciente);
    }

    public Expediente getExpedienteById(Long id) {
        return expedienteRepository.findById(id).orElse(null);
    }

    public Expediente getExpedienteActivo(Integer idPaciente) {
        return expedienteRepository
            .buscarActivoPorPaciente(idPaciente, "ACTIVO")
            .orElse(null);
    }

    public void deleteExpediente(Long id) {
        expedienteRepository.deleteById(id);
    }

    // PUT: Actualiza sin crear nueva versión
    public Expediente updateExpediente(Long id, Expediente actualizado) {
        return expedienteRepository.findById(id).map(existente -> {
            if (actualizado.getFolio() != null)
                existente.setFolio(actualizado.getFolio());
            if (actualizado.getAnt_heredofamiliares() != null)
                existente.setAnt_heredofamiliares(actualizado.getAnt_heredofamiliares());
            if (actualizado.getAnt_patologicos() != null)
                existente.setAnt_patologicos(actualizado.getAnt_patologicos());
            if (actualizado.getAnt_quirurgicos() != null)
                existente.setAnt_quirurgicos(actualizado.getAnt_quirurgicos());
            if (actualizado.getAnt_alergicos() != null)
                existente.setAnt_alergicos(actualizado.getAnt_alergicos());
            if (actualizado.getEnf_cronicas() != null)
                existente.setEnf_cronicas(actualizado.getEnf_cronicas());
            if (actualizado.getAnt_ginecoobstetricos() != null)
                existente.setAnt_ginecoobstetricos(actualizado.getAnt_ginecoobstetricos());
            if (actualizado.getObservaciones() != null)
                existente.setObservaciones(actualizado.getObservaciones());
            return expedienteRepository.save(existente);
        }).orElse(null);
    }

    // PATCH: Crea nueva versión con los cambios (acumulando valores)
    @Transactional
    public Expediente actualizarExpediente(Long idExpediente, Map<String, Object> cambios) {
        Expediente actual = expedienteRepository.findById(idExpediente)
            .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));

        // Desactivar el actual
        actual.setEstado("DESACTIVADO");
        expedienteRepository.save(actual);

        // Clonar expediente
        Expediente nuevo = new Expediente();
        nuevo.setFolio(actual.getFolio());
        nuevo.setAnt_heredofamiliares(actual.getAnt_heredofamiliares());
        nuevo.setAnt_patologicos(actual.getAnt_patologicos());
        nuevo.setAnt_quirurgicos(actual.getAnt_quirurgicos());
        nuevo.setAnt_alergicos(actual.getAnt_alergicos());
        nuevo.setEnf_cronicas(actual.getEnf_cronicas());
        nuevo.setAnt_ginecoobstetricos(actual.getAnt_ginecoobstetricos());
        nuevo.setObservaciones(actual.getObservaciones());
        nuevo.setFechaApertura(actual.getFechaApertura()); // Conserva original
        nuevo.setIdPaciente(actual.getIdPaciente());
        nuevo.setMedico(actual.getMedico());
        nuevo.setEstado("ACTIVO");

        // Aplicar cambios (concatenando)
        aplicarCambios(nuevo, cambios);

        return expedienteRepository.save(nuevo);
    }

    private void aplicarCambios(Expediente nuevo, Map<String, Object> cambios) {
        cambios.forEach((campo, valor) -> {
            if (valor == null) return;
            String valorStr = (String) valor;

            switch (campo) {
                case "ant_heredofamiliares":
                    nuevo.setAnt_heredofamiliares(concatenarSiNoVacio(nuevo.getAnt_heredofamiliares(), valorStr));
                    break;
                case "ant_patologicos":
                    nuevo.setAnt_patologicos(concatenarSiNoVacio(nuevo.getAnt_patologicos(), valorStr));
                    break;
                case "ant_quirurgicos":
                    nuevo.setAnt_quirurgicos(concatenarSiNoVacio(nuevo.getAnt_quirurgicos(), valorStr));
                    break;
                case "ant_alergicos":
                    nuevo.setAnt_alergicos(concatenarSiNoVacio(nuevo.getAnt_alergicos(), valorStr));
                    break;
                case "enf_cronicas":
                    nuevo.setEnf_cronicas(concatenarSiNoVacio(nuevo.getEnf_cronicas(), valorStr));
                    break;
                case "ant_ginecoobstetricos":
                    nuevo.setAnt_ginecoobstetricos(concatenarSiNoVacio(nuevo.getAnt_ginecoobstetricos(), valorStr));
                    break;
                case "observaciones":
                    nuevo.setObservaciones(concatenarSiNoVacio(nuevo.getObservaciones(), valorStr));
                    break;
                case "estado":
                    nuevo.setEstado(valorStr);
                    break;
                case "id_expediente":
                case "id_paciente":
                    throw new RuntimeException("No se puede modificar este campo: " + campo);
                default:
                    throw new RuntimeException("Campo no válido: " + campo);
            }
        });
    }

    private String concatenarSiNoVacio(String actual, String nuevo) {
        if (actual == null || actual.isBlank()) return nuevo;
        if (nuevo == null || nuevo.isBlank()) return actual;
        return actual + "\n" + nuevo;
    }
}