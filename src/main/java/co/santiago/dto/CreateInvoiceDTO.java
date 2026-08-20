package co.santiago.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateInvoiceDTO {

    @NotEmpty
    @Valid
    private List<InvoiceItemRequestDTO> items;
}