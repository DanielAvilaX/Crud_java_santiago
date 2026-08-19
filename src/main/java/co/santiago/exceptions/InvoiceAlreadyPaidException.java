package co.santiago.exceptions;

public class InvoiceAlreadyPaidException extends RuntimeException {

    public InvoiceAlreadyPaidException(Long id) {
        super("La factura con id " + id + " ya se encuentra pagada");
    }
}