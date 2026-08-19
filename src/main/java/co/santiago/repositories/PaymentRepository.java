package co.santiago.repositories;

import co.santiago.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByInvoiceId(Long invoiceId);
}