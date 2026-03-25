package com.hospital.hospital.model.repository;

import com.hospital.hospital.model.entity.Cita;
import com.hospital.hospital.model.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

        // Citas de un paciente
        List<Cita> findByPacienteIdPaciente(Integer idPaciente);

        // Contar citas de un médico en una fecha específica
        long countByMedicoAndFecha(Medico medico, LocalDate fecha);

        // Médicos por turno
        @Query("SELECT m FROM Medico m WHERE m.turno = :turno AND m.activo = true")
        List<Medico> findMedicosByTurno(@Param("turno") Medico.Turno turno);

        // Para CANCELADAS — usa fecha_creacion (LocalDateTime)
        @Modifying
        @Query("UPDATE Cita c SET c.estado = :nuevoEstado " +
                        "WHERE c.estado = :estadoCancelada " +
                        "AND c.fechaCreacion < :hace2Dias")
        void archivarCitasCanceladas(
                        @Param("nuevoEstado") Cita.EstadoCita nuevoEstado,
                        @Param("estadoCancelada") Cita.EstadoCita estadoCancelada,
                        @Param("hace2Dias") LocalDateTime hace2Dias);

        // Para PENDIENTES — usa fecha (LocalDate)
        @Modifying
        @Query("UPDATE Cita c SET c.estado = :nuevoEstado " +
                        "WHERE c.estado = :estadoPendiente " +
                        "AND c.fecha < :hace2Dias")
        void archivarCitasPendientes(
                        @Param("nuevoEstado") Cita.EstadoCita nuevoEstado,
                        @Param("estadoPendiente") Cita.EstadoCita estadoPendiente,
                        @Param("hace2Dias") LocalDate hace2Dias);

        // Para PENDIENTES — usa fecha (LocalDate)
        @Modifying
        @Query("UPDATE Cita c SET c.estado = :nuevoEstado " +
                        "WHERE c.estado = :estadoCompletada " +
                        "AND c.fecha < :hace2Dias")
        void archivarCitasCompletadas(
                        @Param("nuevoEstado") Cita.EstadoCita nuevoEstado,
                        @Param("estadoCompletada") Cita.EstadoCita estadoCompletada,
                        @Param("hace2Dias") LocalDate hace2Dias);

        // Citas por medico
        List<Cita> findByMedicoIdMedico(Integer idMedico);

        // Medico con menos citas del dia de hoy
        @Query("""
                            SELECT c.medico
                            FROM Cita c
                            WHERE c.medico.activo = true
                              AND c.fecha = :fecha
                            GROUP BY c.medico
                            ORDER BY COUNT(c) ASC
                            LIMIT 1
                        """)
        Optional<Medico> findMedicoConMenosCitasHoy(@Param("fecha") LocalDate fecha);

}