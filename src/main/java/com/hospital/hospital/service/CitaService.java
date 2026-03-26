package com.hospital.hospital.service;

import com.hospital.hospital.model.dto.CitaDTO;
import com.hospital.hospital.model.dto.CompletarCitaRequest;
import com.hospital.hospital.model.entity.Cita;
import com.hospital.hospital.model.entity.Diagnostico;
import com.hospital.hospital.model.entity.Medicamento;
import com.hospital.hospital.model.entity.Medico;
import com.hospital.hospital.model.entity.Paciente;
import com.hospital.hospital.model.entity.Receta;
import com.hospital.hospital.model.entity.SignosVitales;
import com.hospital.hospital.model.repository.CitaRepository;
import com.hospital.hospital.model.repository.DiagnosticoRepository;
import com.hospital.hospital.model.repository.MedicamentoRepository;
import com.hospital.hospital.model.repository.MedicoRepository;
import com.hospital.hospital.model.repository.PacienteRepository;
import com.hospital.hospital.model.repository.RecetaRepository;
import com.hospital.hospital.model.repository.SignosVitalesRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final SignosVitalesRepository signosVitalesRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final RecetaRepository recetaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public CitaService(CitaRepository citaRepository,
            SignosVitalesRepository signosVitalesRepository,
            DiagnosticoRepository diagnosticoRepository,
            RecetaRepository recetaRepository,
            MedicamentoRepository medicamentoRepository,
            MedicoRepository medicoRepository,
            PacienteRepository pacienteRepository) {
        this.citaRepository = citaRepository;
        this.signosVitalesRepository = signosVitalesRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.recetaRepository = recetaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    // ✅ Devuelve el turno principal + jornada_acumulada siempre
    private List<Medico.Turno> getTurnosParaHora(LocalTime hora) {
        Medico.Turno turnoPrincipal;

        if (hora.isBefore(LocalTime.of(14, 0))) {
            turnoPrincipal = Medico.Turno.matutino;
        } else if (hora.isBefore(LocalTime.of(20, 0))) {
            turnoPrincipal = Medico.Turno.vespertino;
        } else {
            turnoPrincipal = Medico.Turno.nocturno;
        }

        return List.of(turnoPrincipal, Medico.Turno.jornada_acumulada);
    }

    // ✅ Ahora usa medicoRepository directamente
    private Medico asignarMedico(LocalDate fecha, LocalTime hora) {
        List<Medico.Turno> turnos = getTurnosParaHora(hora);
        List<Medico> medicos = medicoRepository.findByTurnosAndActivoTrue(turnos);

        if (medicos.isEmpty()) {
            throw new RuntimeException(
                    "No hay médicos disponibles para el horario: " + hora);
        }

        return medicos.stream()
                .min(Comparator.comparingLong(m -> citaRepository.countByMedicoAndFecha(m, fecha)))
                .orElseThrow(() -> new RuntimeException("No se pudo asignar un médico"));
    }

    // ✅ Parámetro renombrado a idUsuario para mayor claridad
    @Transactional
    public Cita agendarCita(Integer idUsuario, LocalDate fecha, LocalTime hora,
            String motivo, Cita.TipoCita tipo) {

        Paciente paciente = pacienteRepository.obtenerConUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado para idUsuario: " + idUsuario));

        Medico medico = asignarMedico(fecha, hora);

        Cita cita = new Cita();
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo);
        cita.setTipo(tipo);
        cita.setEstado(Cita.EstadoCita.pendiente);
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFolio(generarFolio());
        cita.setFechaCreacion(java.time.LocalDateTime.now());

        return citaRepository.save(cita);
    }

    public List<Cita> obtenerCitasPorPaciente(Integer idPaciente) {
        return citaRepository.findByPacienteIdPaciente(idPaciente);
    }

    // ✅ Nuevo método — recibe idUsuario y resuelve idPaciente internamente
    public List<Cita> obtenerCitasPorIdUsuario(Integer idUsuario) {
        Paciente paciente = pacienteRepository.obtenerConUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado para idUsuario: " + idUsuario));
        return citaRepository.findByPacienteIdPaciente(paciente.getIdPaciente());
    }

    @Transactional
    public Cita cancelarCita(Integer idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (cita.getEstado() == Cita.EstadoCita.completada) {
            throw new RuntimeException("No se puede cancelar una cita completada");
        }

        cita.setEstado(Cita.EstadoCita.cancelada);
        return citaRepository.save(cita);
    }

    private String generarFolio() {
        return "CITA-" + System.currentTimeMillis();
    }

    public List<CitaDTO> getCitasByMedico(Integer idUsuario) {
        Medico medico = medicoRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        return citaRepository.findByMedicoIdMedico(medico.getIdMedico())
                .stream()
                .map(CitaDTO::new)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void completarCita(Integer idCita, CompletarCitaRequest request) {

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + idCita));

        CompletarCitaRequest.SignosVitalesDTO svDTO = request.getSignosVitales();
        if (svDTO != null) {
            SignosVitales sv = new SignosVitales();
            sv.setPesoKg(svDTO.getPesoKg());
            sv.setTallaM(svDTO.getTallaM());
            sv.setPresionArterial(svDTO.getPresionArterial());
            sv.setFrecuenciaCardiaca(svDTO.getFrecuenciaCardiaca());
            sv.setFrecuenciaRespiratoria(svDTO.getFrecuenciaRespiratoria());
            sv.setTemperatura(svDTO.getTemperatura());
            sv.setSpo2(svDTO.getSpo2());
            sv.setGlucosa(svDTO.getGlucosa());
            sv.setIdCita(idCita.longValue());
            signosVitalesRepository.save(sv);
        }

        CompletarCitaRequest.DiagnosticoDTO dxDTO = request.getDiagnostico();
        if (dxDTO != null) {
            Diagnostico dx = new Diagnostico();
            dx.setCie10(dxDTO.getCie10());
            dx.setDescripcion(dxDTO.getDescripcion());
            dx.setTipo(dxDTO.getTipo());
            dx.setMedicamentos_base(dxDTO.getMedicamentosBase());
            dx.setTratamiento(dxDTO.getTratamiento());
            dx.setIndicaciones(dxDTO.getIndicaciones());
            dx.setFun_alta(dxDTO.getFunAlta());
            dx.setIdCita(idCita.longValue());
            diagnosticoRepository.save(dx);
        }

        CompletarCitaRequest.RecetaDTO recetaDTO = request.getReceta();
        if (recetaDTO != null) {
            Receta receta = new Receta();
            receta.setFolio(recetaDTO.getFolio());
            receta.setFecha(LocalDate.now());
            receta.setVencimiento(recetaDTO.getVencimiento());
            receta.setEstado(Receta.EstadoReceta.activa);
            receta.setCita(cita);
            Receta recetaGuardada = recetaRepository.save(receta);

            if (recetaDTO.getMedicamentos() != null) {
                for (CompletarCitaRequest.MedicamentoDTO medDTO : recetaDTO.getMedicamentos()) {
                    Medicamento med = new Medicamento();
                    med.setNombre(medDTO.getNombre());
                    med.setPresentacion(medDTO.getPresentacion());
                    med.setDosis(medDTO.getDosis());
                    med.setFrecuencia(medDTO.getFrecuencia());
                    med.setDuracion(medDTO.getDuracion());
                    med.setCantidad(medDTO.getCantidad());
                    med.setVia(medDTO.getVia());
                    med.setReceta(recetaGuardada);
                    medicamentoRepository.save(med);
                }
            }
        }

        cita.setEstado(Cita.EstadoCita.completada);
        citaRepository.save(cita);
    }
}