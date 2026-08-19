package co.santiago.dto;

import lombok.Data;

@Data
public class ItemRequestDTO {

    private String nombre;
    private String descripcion;
    private Integer precio;
}