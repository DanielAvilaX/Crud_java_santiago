package co.santiago.dto;

import lombok.Data;

@Data
public class ItemsDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String precioUnidad;
}