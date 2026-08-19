package co.santiago.services;

import co.santiago.dto.PaymentDTO;
import co.santiago.dto.PaymentRequestDTO;

public interface PaymentService {

    PaymentDTO createPayment(PaymentRequestDTO paymentRequestDTO);
}