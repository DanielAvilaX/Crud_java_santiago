package co.santiago.exceptions;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item no encontrado con id: " + id);
    }
}