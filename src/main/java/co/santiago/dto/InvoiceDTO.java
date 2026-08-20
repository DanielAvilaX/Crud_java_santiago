package co.santiago.dto;

import co.santiago.enums.InvoiceStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceDTO {

    private Long id;
    private LocalDateTime fecha;
    private List<InvoiceItemDTO> items;
    private InvoiceStatus estado;
    private String TotalFormateado;
    private Integer totalProductos;
}