package com.hospital.hospital.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.hospital.model.entity.Expediente;
import com.hospital.hospital.model.entity.Paciente;
import com.hospital.hospital.model.repository.ExpedienteRepository;
import com.hospital.hospital.model.repository.PacienteRepository;
import com.hospital.hospital.util.JwtUtil;


@Service
public class ExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final PacienteRepository pacienteRepository;

    public ExpedienteService(ExpedienteRepository expedienteRepository,
                             PacienteRepository pacienteRepository) {
        this.expedienteRepository = expedienteRepository;
        this.pacienteRepository = pacienteRepository;
    }

    //Guardar expediente
    public Expediente saveExpediente(Expediente expediente) {

        Integer idUsuario = JwtUtil.getIdUsuario();

        // Buscar paciente por id_usuario
        Paciente idPaciente = pacienteRepository
                .obtenerConUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        expediente.setIdPaciente(idPaciente);

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
            .findByIdPacienteAndEstado(idPaciente, "ACTIVO")
            .orElseThrow(() -> new RuntimeException("No hay expediente activo"));
    }

    public void deleteExpediente(Long id) {
        expedienteRepository.deleteById(id);
    }

    //Update SIN tocar paciente 
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

    @Transactional
    public Expediente actualizarExpediente(Long idExpediente, Map<String, Object> cambios) {

        //Obtener expediente actual
        Expediente actual = expedienteRepository.findById(idExpediente)
            .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));

        //Desactivar el actual
        actual.setEstado("DESACTIVADO");
        expedienteRepository.save(actual);

        //Clonar expediente
        Expediente nuevo = new Expediente();

        nuevo.setFolio(actual.getFolio());
        nuevo.setAnt_heredofamiliares(actual.getAnt_heredofamiliares());
        nuevo.setAnt_patologicos(actual.getAnt_patologicos());
        nuevo.setAnt_quirurgicos(actual.getAnt_quirurgicos());
        nuevo.setAnt_alergicos(actual.getAnt_alergicos());
        nuevo.setEnf_cronicas(actual.getEnf_cronicas());
        nuevo.setAnt_ginecoobstetricos(actual.getAnt_ginecoobstetricos());
        nuevo.setObservaciones(actual.getObservaciones());
        nuevo.setFechaApertura(actual.getFechaApertura());
        nuevo.setIdPaciente(actual.getIdPaciente());
        nuevo.setIdMedico(actual.getIdMedico());

        nuevo.setEstado("ACTIVO");

        // Aplicar cambios dinámicos
        aplicarCambios(nuevo, cambios);

        return expedienteRepository.save(nuevo);
    } 

    private void aplicarCambios(Expediente nuevo, Map<String, Object> cambios) {

        cambios.forEach((campo, valor) -> {

            switch (campo) {

                case "ant_heredofamiliares":
                    nuevo.setAnt_heredofamiliares((String) valor);
                    break;

                case "ant_patologicos":
                    nuevo.setAnt_patologicos((String) valor);
                    break;

                case "ant_quirurgicos":
                    nuevo.setAnt_quirurgicos((String) valor);
                    break;

                case "ant_alergicos":
                    nuevo.setAnt_alergicos((String) valor);
                    break;

                case "enf_cronicas":
                     nuevo.setEnf_cronicas((String) valor); 
                    break;

                case "ant_ginecoobstetricos":
                    nuevo.setAnt_ginecoobstetricos((String) valor);
                    break;

                case "observaciones":
                    nuevo.setObservaciones((String) valor);
                    break;

                case "estado":
                    nuevo.setEstado((String) valor);
                    break;

                // NO modificar estos
                case "id_expediente":
                case "id_paciente":
                    throw new RuntimeException("No se puede modificar este campo: " + campo);

                default:
                    throw new RuntimeException("Campo no válido: " + campo);
            }
        });
    }
}