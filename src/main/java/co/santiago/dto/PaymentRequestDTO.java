package co.santiago.dto;

import co.santiago.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDTO {

    private Long invoiceId;

    private PaymentMethod metodoPago;

}