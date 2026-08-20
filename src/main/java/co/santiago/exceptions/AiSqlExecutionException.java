package co.santiago.exceptions;

public class AiSqlExecutionException
        extends RuntimeException {

    public AiSqlExecutionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}