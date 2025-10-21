package co.santiago.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class ItemsDTO {
    private Long id;
    @NonNull
    private String nombre;
    @NonNull
    private Integer precio;
}
