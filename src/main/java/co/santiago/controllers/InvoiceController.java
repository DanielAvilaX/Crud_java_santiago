package co.santiago.controllers;

import co.santiago.dto.CreateInvoiceDTO;
import co.santiago.dto.InvoiceDTO;
import co.santiago.services.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(
            @RequestBody CreateInvoiceDTO createInvoiceDTO
    ) {

        InvoiceDTO invoiceDTO =
                invoiceService.createInvoice(createInvoiceDTO);

        return ResponseEntity.ok(invoiceDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceById(id)
        );
    }
    @PutMapping("/{id}/estado")
    public ResponseEntity<InvoiceDTO> updateEstado(
            @PathVariable Long id,
            @RequestParam String estado
    ) {
        return ResponseEntity.ok(
                invoiceService.updateEstado(id, estado)
        );
    }
}