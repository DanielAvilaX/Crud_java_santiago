package co.santiago.dto;

import co.santiago.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDTO {

    private Long id;

    private Long invoiceId;

    private String montoFormateado;

    private PaymentMethod metodoPago;

    private String referencia;

    private LocalDateTime fechaPago;

    private String estadoFactura;
}