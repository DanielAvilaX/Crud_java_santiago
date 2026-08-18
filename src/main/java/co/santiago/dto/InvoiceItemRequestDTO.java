package co.santiago.dto;

import lombok.Data;

@Data
public class InvoiceItemRequestDTO {

    private Long itemId;
    private Integer cantidad;
}
