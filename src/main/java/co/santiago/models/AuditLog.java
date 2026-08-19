package co.santiago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidad;

    private Long entidadId;

    private String accion;

    private String usuario;

    private LocalDateTime fecha;

    @Lob
    private String valorAnterior;

    @Lob
    private String valorNuevo;
}