package co.santiago.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
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
    @ExceptionHandler(InvoiceAlreadyPaidException.class)
    public ProblemDetail handleInvoiceAlreadyPaid(
            InvoiceAlreadyPaidException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Factura ya pagada");
        problem.setDetail(ex.getMessage());

        return problem;
    }
    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFound(
            PaymentNotFoundException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Pago no encontrado");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(
            MethodArgumentNotValidException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Datos inválidos");
        problem.setDetail(
                ex.getBindingResult()
                        .getFieldError()
                        .getDefaultMessage()
        );

        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Parámetro inválido");
        problem.setDetail(
                "El valor '" + ex.getValue() + "' no es válido para "
                        + ex.getName()
        );

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Conflicto de datos");
        problem.setDetail(
                "La operación no se pudo completar por un conflicto en los datos"
        );

        return problem;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex
    ) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Conflicto de concurrencia");
        problem.setDetail(
                "El recurso fue modificado por otro proceso, intenta de nuevo"
        );

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {

        log.error("Error inesperado", ex);

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problem.setTitle("Error interno");
        problem.setDetail("Ocurrió un error inesperado");

        return problem;
    }
}