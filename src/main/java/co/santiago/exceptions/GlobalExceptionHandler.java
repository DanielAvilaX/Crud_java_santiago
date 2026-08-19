package co.santiago.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public ProblemDetail handleItemNotFound(
            ItemNotFoundException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Item no encontrado");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(ItemInactiveException.class)
    public ProblemDetail handleItemInactive(
            ItemInactiveException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Item eliminado");
        problem.setDetail(ex.getMessage());

        return problem;
    }
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ProblemDetail handleInvoiceNotFound(
            InvoiceNotFoundException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Factura no encontrada");
        problem.setDetail(ex.getMessage());

        return problem;
    }
}