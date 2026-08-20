package co.santiago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;

    @NotNull
    @Positive
    private Integer precio;
}