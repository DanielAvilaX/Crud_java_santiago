package co.santiago.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceDTO {

    private Long id;
    private Integer total;
    private LocalDateTime fecha;
    private List<InvoiceItemDTO> items;
    private String estado;
}