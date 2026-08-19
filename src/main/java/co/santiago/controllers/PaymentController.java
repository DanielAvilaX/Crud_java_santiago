package co.santiago.controllers;

import co.santiago.dto.PaymentDTO;
import co.santiago.dto.PaymentRequestDTO;
import co.santiago.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
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
            summary = "Pagar factura"
    )
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(
            @RequestBody PaymentRequestDTO paymentRequestDTO
    ) {

        return ResponseEntity.ok(
                paymentService.createPayment(paymentRequestDTO)
        );
    }
}