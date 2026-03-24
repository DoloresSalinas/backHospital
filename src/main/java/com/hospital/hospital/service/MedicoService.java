package com.hospital.hospital.service;

import com.hospital.hospital.model.dto.MedicoDTO;
import com.hospital.hospital.model.entity.Medico;
import com.hospital.hospital.model.entity.UnidadMedica;
import com.hospital.hospital.model.entity.Usuario;
import com.hospital.hospital.model.repository.CitaRepository;
import com.hospital.hospital.model.repository.MedicoRepository;
import com.hospital.hospital.model.repository.UnidadMedicaRepository;
import com.hospital.hospital.model.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final UnidadMedicaRepository unidadMedicaRepository;
    private final CitaRepository citaRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Medico registrarMedico(String correo, String contrasena, String nombre,
            String apPaterno, String apMaterno, String especialidad,
            String cedulaProfesional, String numEmpleado,
            Medico.Turno turno, String consultorio, Short idUm) {

        if (usuarioRepository.existsByCorreo(correo))
            throw new RuntimeException("El correo ya está registrado: " + correo);

        if (medicoRepository.existsByCedulaProfesional(cedulaProfesional))
            throw new RuntimeException("La cédula profesional ya está registrada: " + cedulaProfesional);

        if (medicoRepository.existsByNumEmpleado(numEmpleado))
            throw new RuntimeException("El número de empleado ya está registrado: " + numEmpleado);

        UnidadMedica unidadMedica = unidadMedicaRepository.findById(idUm)
                .orElseThrow(() -> new RuntimeException("Unidad médica no encontrada: " + idUm));

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setRol(Usuario.Rol.MEDICO);
        usuario.setEstado(Usuario.Estado.ACTIVO);
        usuarioRepository.save(usuario);

        Medico medico = new Medico();
        medico.setNumEmpleado(numEmpleado);
        medico.setCedulaProfesional(cedulaProfesional);
        medico.setNombre(nombre);
        medico.setApPaterno(apPaterno);
        medico.setApMaterno(apMaterno);
        medico.setEspecialidad(especialidad);
        medico.setTurno(turno);
        medico.setConsultorio(consultorio);
        medico.setActivo(true);
        medico.setUsuario(usuario);
        medico.setUnidadMedica(unidadMedica);

        return medicoRepository.save(medico);
    }

    // Ver MI perfil
    public Medico getMiPerfil(Integer idUsuario) {
        return medicoRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
    }

    // Editar MI perfil — solo campos no nulos
    @Transactional
    public Medico editarPerfil(Integer idUsuario, MedicoDTO dto) {
        Medico medico = medicoRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        if (dto.getNombre() != null)
            medico.setNombre(dto.getNombre());
        if (dto.getApPaterno() != null)
            medico.setApPaterno(dto.getApPaterno());
        if (dto.getApMaterno() != null)
            medico.setApMaterno(dto.getApMaterno());
        if (dto.getEspecialidad() != null)
            medico.setEspecialidad(dto.getEspecialidad());
        if (dto.getTurno() != null)
            medico.setTurno(dto.getTurno());
        if (dto.getConsultorio() != null)
            medico.setConsultorio(dto.getConsultorio());
        if (dto.getCedulaProfesional() != null) {
            if (medicoRepository.existsByCedulaProfesional(dto.getCedulaProfesional())
                    && !medico.getCedulaProfesional().equals(dto.getCedulaProfesional()))
                throw new RuntimeException("La cédula profesional ya está en uso");
            medico.setCedulaProfesional(dto.getCedulaProfesional());
        }
        if (dto.getIdUm() != null) {
            UnidadMedica unidadMedica = unidadMedicaRepository.findById(dto.getIdUm())
                    .orElseThrow(() -> new RuntimeException("Unidad médica no encontrada"));
            medico.setUnidadMedica(unidadMedica);
        }

        return medicoRepository.save(medico);
    }

    // Borrado lógico — desactiva el usuario y el médico
    @Transactional
    public void desactivarMedico(Integer idUsuario) {
        Medico medico = medicoRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        medico.setActivo(false);
        medico.getUsuario().setEstado(Usuario.Estado.INACTIVO);
        medicoRepository.save(medico);
        usuarioRepository.save(medico.getUsuario());
    }


    public Medico getMedicoConMenosCitas() {
    return citaRepository.findMedicoConMenosCitasHoy(LocalDate.now())
            .orElseThrow(() -> new RuntimeException(
                "No hay médicos con citas registradas hoy"));
}
}