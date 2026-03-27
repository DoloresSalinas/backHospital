package com.hospital.hospital.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.hospital.hospital.model.entity.Expediente;

public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {

    List<Expediente> findByIdPaciente_IdPaciente(Integer idPaciente);
    
    @Query("SELECT e FROM Expediente e WHERE e.idPaciente.idPaciente = :idPaciente AND e.estado = :estado")
    Optional<Expediente> buscarActivoPorPaciente(@Param("idPaciente") Integer idPaciente, @Param("estado") String estado);
}