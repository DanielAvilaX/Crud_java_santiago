package co.santiago.exceptions;

public class InvoiceAlreadyPaidException extends RuntimeException {

    public InvoiceAlreadyPaidException(Long id) {
        super("La factura con id " + id + " ya tiene pagos registrados y no se puede modificar");
    }
}