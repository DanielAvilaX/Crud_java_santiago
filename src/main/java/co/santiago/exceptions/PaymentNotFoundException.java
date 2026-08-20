package co.santiago.exceptions;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long invoiceId) {
        super("No se han registrado pagos para la factura con id: " + invoiceId);
    }
}
