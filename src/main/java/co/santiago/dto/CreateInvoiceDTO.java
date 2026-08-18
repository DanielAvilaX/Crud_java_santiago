package co.santiago.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateInvoiceDTO {

    private List<InvoiceItemRequestDTO> items;
}