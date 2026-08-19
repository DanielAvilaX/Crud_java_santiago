package co.santiago.exceptions;

public class ItemInactiveException extends RuntimeException {

    public ItemInactiveException(Long id) {
        super("El item con id " + id + " está inactivo");
    }
}