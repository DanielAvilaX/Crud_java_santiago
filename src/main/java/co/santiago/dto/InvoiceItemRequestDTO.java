package co.santiago.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InvoiceItemRequestDTO {

    @NotNull
    private Long itemId;

    @NotNull
    @Positive
    private Integer cantidad;
}
