package com.hospital.hospital.scheduler;

import com.hospital.hospital.model.entity.Cita;
import com.hospital.hospital.model.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaScheduler {

    private final CitaRepository citaRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void archivarCitasAntiguas() {

        // Canceladas: 2 días desde que se canceló (fecha_creacion)
        LocalDateTime hace2DiasDateTime = LocalDateTime.now().minusDays(2);

        // Pendientes: 2 días desde la fecha de la cita (fecha)
        LocalDate hace2DiasDate = LocalDate.now().minusDays(2);

        // Completadas: 2 días desde la fecha de la cita (fecha)
        LocalDate hace2DiasDateTimeTime = LocalDate.now().minusDays(2);

        log.info(">>> Archivando citas canceladas anteriores a: {}", hace2DiasDateTime);
        citaRepository.archivarCitasCanceladas(
                Cita.EstadoCita.archivada,
                Cita.EstadoCita.cancelada,
                hace2DiasDateTime);

        log.info(">>> Archivando citas pendientes con fecha anterior a: {}", hace2DiasDate);
        citaRepository.archivarCitasPendientes(
                Cita.EstadoCita.archivada,
                Cita.EstadoCita.pendiente,
                hace2DiasDate);

        log.info(">>> Archivando citas completadas con fecha anterior a: {}", hace2DiasDateTimeTime);
        citaRepository.archivarCitasCompletadas(
                Cita.EstadoCita.archivada,
                Cita.EstadoCita.completada,
                hace2DiasDateTimeTime);

        log.info(">>> Archivado completado");
    }
}