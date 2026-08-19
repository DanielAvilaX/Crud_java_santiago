package co.santiago.exceptions;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(Long id) {
        super("Factura no encontrada con id: " + id);
    }
}