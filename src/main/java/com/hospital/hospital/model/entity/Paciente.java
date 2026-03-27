package com.hospital.hospital.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hospital.hospital.model.converter.TipoSangreConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer idPaciente;

    @Column(name = "nss", length = 11, nullable = false, unique = true)
    private String nss;

    @Column(name = "curp", length = 18, nullable = false, unique = true)
    private String curp;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "ap_paterno", length = 100, nullable = false)
    private String apPaterno;

    @Column(name = "ap_materno", length = 100, nullable = false)
    private String apMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    // Sin @Enumerated — el converter maneja la conversión JPA <-> BD
    @Convert(converter = TipoSangreConverter.class)
    @Column(name = "tipo_sangre")
    private TipoSangre tipoSangre;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "tel_emergencia", length = 150)
    private String telefonoEmergencias;

    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;

    @PrePersist
    public void prePersist() {
        if (this.fechaAlta == null) {
        this.fechaAlta = LocalDateTime.now();
        }
    }

    @ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "id_direccion")
    private Direccion direccion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_um")
    private UnidadMedica unidadMedica;

    public enum Sexo {
        M, F
    }

    public enum TipoSangre {
        A_POS("A+"), A_NEG("A-"),
        B_POS("B+"), B_NEG("B-"),
        AB_POS("AB+"), AB_NEG("AB-"),
        O_POS("O+"), O_NEG("O-");

        private final String valor;

        TipoSangre(String valor) {
            this.valor = valor;
        }

        // Serializa a JSON como "O+" en lugar de "O_POS"
        @JsonValue
        public String getValor() {
            return valor;
        }

        // Acepta "O+", "AB-", etc. desde el JSON entrante
        @JsonCreator
        public static TipoSangre fromValor(String v) {
            for (TipoSangre ts : values()) {

                if (ts.valor.equalsIgnoreCase(v))
                    return ts;

            }
            throw new IllegalArgumentException("Tipo de sangre inválido: " + v);
        }
    }
}