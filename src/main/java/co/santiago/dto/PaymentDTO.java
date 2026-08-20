package co.santiago.dto;

import co.santiago.enums.InvoiceStatus;
import co.santiago.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDTO {

    private Long id;

    private Long invoiceId;

    private String montoPagadoFormateado;

    private PaymentMethod metodoPago;

    private String referencia;

    private LocalDateTime fechaPago;

    private InvoiceStatus estadoFactura;

    private String saldoPendienteFormateado;

    private String saldoAFavorFormateado;
}