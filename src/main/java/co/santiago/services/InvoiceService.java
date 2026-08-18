package co.santiago.services;

import co.santiago.dto.CreateInvoiceDTO;
import co.santiago.dto.InvoiceDTO;

public interface InvoiceService {

    InvoiceDTO createInvoice(CreateInvoiceDTO createInvoiceDTO);

    InvoiceDTO getInvoiceById(Long id);

    InvoiceDTO updateEstado(Long id, String estado);
}