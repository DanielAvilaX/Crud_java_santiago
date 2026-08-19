package co.santiago.models;

import co.santiago.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer monto;

    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    private PaymentMethod metodoPago;

    private String referencia;

    @OneToOne
    @JoinColumn(
            name = "invoice_id",
            nullable = false,
            unique = true
    )
    private Invoice invoice;
}