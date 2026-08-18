package co.santiago.dto;

import lombok.Data;

@Data
public class InvoiceItemDTO {

    private Long id;
    private String nombre;
    private Integer precio;
    private Integer cantidad;
}