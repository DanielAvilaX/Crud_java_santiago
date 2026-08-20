package co.santiago.controllers;

import co.santiago.dto.PaymentDTO;
import co.santiago.dto.PaymentRequestDTO;
import co.santiago.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Pagar o abonar a una factura"
    )
    @PutMapping("/{invoiceId}")
    public ResponseEntity<PaymentDTO> createPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequestDTO paymentRequestDTO
    ) {

        return ResponseEntity.ok(
                paymentService.createPayment(invoiceId, paymentRequestDTO)
        );
    }

    @Operation(
            summary = "Consultar estado de pago de una factura"
    )
    @GetMapping("/{invoiceId}")
    public ResponseEntity<PaymentDTO> getPaymentByInvoiceId(
            @PathVariable Long invoiceId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentByInvoiceId(invoiceId)
        );
    }
}