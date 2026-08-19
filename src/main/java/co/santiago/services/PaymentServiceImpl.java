package co.santiago.services;

import co.santiago.dto.PaymentDTO;
import co.santiago.dto.PaymentRequestDTO;
import co.santiago.enums.AuditAction;
import co.santiago.exceptions.InvoiceAlreadyPaidException;
import co.santiago.exceptions.InvoiceNotFoundException;
import co.santiago.models.Invoice;
import co.santiago.models.Payment;
import co.santiago.repositories.InvoiceRepository;
import co.santiago.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentRequestDTO paymentRequestDTO) {

        Payment existingPayment = paymentRepository
                .findByInvoiceId(paymentRequestDTO.getInvoiceId())
                .orElse(null);

        if (existingPayment != null) {
            return convertToDTO(existingPayment);
        }

        Invoice invoice = invoiceRepository
                .findById(paymentRequestDTO.getInvoiceId())
                .orElseThrow(() ->
                        new InvoiceNotFoundException(
                                paymentRequestDTO.getInvoiceId()
                        )
                );

        Payment payment = new Payment();

        payment.setInvoice(invoice);
        payment.setMonto(invoice.getTotal());
        payment.setFechaPago(LocalDateTime.now());
        payment.setMetodoPago(
                paymentRequestDTO.getMetodoPago()
        );

        String referencia = "PAY-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        payment.setReferencia(referencia);

        Payment savedPayment =
                paymentRepository.saveAndFlush(payment);

        String estadoAnterior = invoice.getEstado();

        invoice.setEstado("PAGADA");

        invoiceRepository.saveAndFlush(invoice);

        auditService.log(
                "Payment",
                savedPayment.getId(),
                AuditAction.CREATE,
                "santiago",
                null,
                savedPayment
        );

        auditService.log(
                "Invoice",
                invoice.getId(),
                AuditAction.UPDATE,
                "santiago",
                estadoAnterior,
                invoice.getEstado()
        );

        return convertToDTO(savedPayment);
    }    private PaymentDTO convertToDTO(Payment payment) {

        PaymentDTO dto = new PaymentDTO();

        dto.setId(payment.getId());
        dto.setInvoiceId(
                payment.getInvoice().getId()
        );
        dto.setMontoFormateado(
                formatPrecio(payment.getMonto())
        );
        dto.setMetodoPago(
                payment.getMetodoPago()
        );
        dto.setReferencia(
                payment.getReferencia()
        );
        dto.setFechaPago(
                payment.getFechaPago()
        );
        dto.setEstadoFactura(
                payment.getInvoice().getEstado()
        );

        return dto;
    }

    private String formatPrecio(Integer precio) {
        return String.format("$%,d", precio)
                .replace(",", ".");
    }
}