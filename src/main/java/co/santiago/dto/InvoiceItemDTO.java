package co.santiago.dto;

import lombok.Data;

@Data
public class InvoiceItemDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    private String precioUnidad;

    private Integer cantidad;

    private String totalProducto;
}