package co.santiago.controllers;

import co.santiago.dto.CreateInvoiceDTO;
import co.santiago.dto.InvoiceDTO;
import co.santiago.dto.InvoiceItemRequestDTO;
import co.santiago.services.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }
    @Operation(
        summary = "Crear factura"
    )
    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(
            @RequestBody CreateInvoiceDTO createInvoiceDTO
    ) {

        InvoiceDTO invoiceDTO =
                invoiceService.createInvoice(createInvoiceDTO);

        return ResponseEntity.ok(invoiceDTO);
    }
    @Operation(
            summary = "Buscar factura"
    )
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceById(id)
        );
    }@Operation(
            summary = "Actualizar estado factura"
    )
    @PutMapping("/{id}/estado")
    public ResponseEntity<InvoiceDTO> updateEstado(
            @PathVariable Long id,
            @RequestParam String estado
    ) {
        return ResponseEntity.ok(
                invoiceService.updateEstado(id, estado)
        );
    }
    @Operation(
            summary = "Agregar productos a una factura"
    )
    @PutMapping("/{id}/items")
    public ResponseEntity<InvoiceDTO> addItem(
            @PathVariable Long id,
            @RequestBody InvoiceItemRequestDTO itemRequest
    ) {

        return ResponseEntity.ok(
                invoiceService.addItem(id, itemRequest)
        );
    }
}