package com.hospital.hospital.model.repository;

import com.hospital.hospital.model.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    boolean existsByCedulaProfesional(String cedulaProfesional);

    boolean existsByNumEmpleado(String numEmpleado);

    Optional<Medico> findByUsuarioIdUsuario(Integer idUsuario);

    @Query("SELECT m FROM Medico m WHERE m.turno IN :turnos AND m.activo = true")
    List<Medico> findByTurnosAndActivoTrue(@Param("turnos") List<Medico.Turno> turnos);
}
