package co.santiago.services;

import co.santiago.dto.CreateInvoiceDTO;
import co.santiago.dto.InvoiceDTO;
import co.santiago.dto.InvoiceItemRequestDTO;

public interface InvoiceService {

    InvoiceDTO createInvoice(CreateInvoiceDTO createInvoiceDTO);

    InvoiceDTO getInvoiceById(Long id);

    InvoiceDTO addItem(Long InvoiceId, InvoiceItemRequestDTO itemRequest);
}