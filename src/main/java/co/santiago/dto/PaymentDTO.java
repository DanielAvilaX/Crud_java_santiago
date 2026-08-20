package co.santiago.dto;

import co.santiago.enums.InvoiceStatus;
import co.santiago.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDTO {

    private Long id;

    private Long invoiceId;

    private String montoPagado;

    private PaymentMethod metodoPago;

    private String referencia;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "dd/MM/yyyy HH:mm",
            timezone = "America/Bogota"
    )
    private LocalDateTime fechaPago;

    private InvoiceStatus estadoFactura;

    private String saldoPendiente;

    private String saldoAFavor;
}