package co.santiago.dto;

import co.santiago.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDTO {

    @NotNull
    @Positive
    private Integer monto;

    @NotNull
    private PaymentMethod metodoPago;

}