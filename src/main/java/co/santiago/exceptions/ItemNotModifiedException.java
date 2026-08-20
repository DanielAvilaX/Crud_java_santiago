package co.santiago.exceptions;

public class ItemNotModifiedException extends RuntimeException {

    public ItemNotModifiedException(Long id) {
        super(
                "El item con id " + id +
                        " ya contiene los valores enviados"
        );
    }
}