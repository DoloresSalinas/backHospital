package com.hospital.hospital.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hospital.hospital.model.entity.Expediente;

public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {

    List<Expediente> findByIdPaciente_IdPaciente(Integer idPaciente);
    Optional<Expediente> findByIdPacienteAndEstado(Integer idPaciente, String estado);

}