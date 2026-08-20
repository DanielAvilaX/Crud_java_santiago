package co.santiago.services;

import co.santiago.dto.PaymentDTO;
import co.santiago.dto.PaymentRequestDTO;
import co.santiago.enums.AuditAction;
import co.santiago.enums.InvoiceStatus;
import co.santiago.exceptions.InvoiceAlreadyPaidException;
import co.santiago.exceptions.InvoiceNotFoundException;
import co.santiago.exceptions.PaymentNotFoundException;
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
    public PaymentDTO createPayment(
            Long invoiceId,
            PaymentRequestDTO paymentRequestDTO
    ) {

        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(invoiceId)
                );

        if (invoice.getEstado() == InvoiceStatus.PAGADA) {
            throw new InvoiceAlreadyPaidException(invoice.getId());
        }

        Payment existingPayment = paymentRepository
                .findByInvoiceId(invoiceId)
                .orElse(null);

        int montoAcumulado = paymentRequestDTO.getMonto();

        Payment payment;

        if (existingPayment != null) {

            montoAcumulado += existingPayment.getMonto();

            existingPayment.setMonto(montoAcumulado);
            existingPayment.setMetodoPago(paymentRequestDTO.getMetodoPago());
            existingPayment.setFechaPago(LocalDateTime.now());

            payment = existingPayment;

        } else {

            payment = new Payment();

            payment.setInvoice(invoice);
            payment.setMonto(montoAcumulado);
            payment.setFechaPago(LocalDateTime.now());
            payment.setMetodoPago(paymentRequestDTO.getMetodoPago());

            String referencia = "PAY-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

            payment.setReferencia(referencia);
        }

        Payment savedPayment =
                paymentRepository.saveAndFlush(payment);

        InvoiceStatus estadoAnterior = invoice.getEstado();

        if (montoAcumulado < invoice.getTotal()) {
            invoice.setEstado(InvoiceStatus.PAGO_PARCIAL);
        } else {
            invoice.setEstado(InvoiceStatus.PAGADA);
        }

        invoiceRepository.saveAndFlush(invoice);

        auditService.log(
                "Payment",
                savedPayment.getId(),
                existingPayment != null ? AuditAction.UPDATE : AuditAction.CREATE,
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
    }

    @Override
    public PaymentDTO getPaymentByInvoiceId(Long invoiceId) {

        invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(invoiceId)
                );

        Payment payment = paymentRepository
                .findByInvoiceId(invoiceId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(invoiceId)
                );

        return convertToDTO(payment);
    }

    private PaymentDTO convertToDTO(Payment payment) {

        PaymentDTO dto = new PaymentDTO();

        dto.setId(payment.getId());
        dto.setInvoiceId(
                payment.getInvoice().getId()
        );
        dto.setMontoPagadoFormateado(
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

        int diferencia = payment.getMonto() - payment.getInvoice().getTotal();

        if (diferencia < 0) {
            dto.setSaldoPendienteFormateado(formatPrecio(-diferencia));
        } else if (diferencia > 0) {
            dto.setSaldoAFavorFormateado(formatPrecio(diferencia));
        }

        return dto;
    }

    private String formatPrecio(Integer precio) {
        return String.format("$%,d", precio)
                .replace(",", ".");
    }
}