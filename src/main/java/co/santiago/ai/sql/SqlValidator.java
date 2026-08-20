package co.santiago.ai.sql;

import co.santiago.exceptions.InvalidAiSqlException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SqlValidator {

    private static final Pattern FORBIDDEN_KEYWORDS =
            Pattern.compile(
                    "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|MERGE|REPLACE|GRANT|REVOKE|CALL)\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern SQL_COMMENT =
            Pattern.compile("(--|/\\*|\\*/)");

    public void validate(String sql) {

        if (sql == null || sql.isBlank()) {
            throw new InvalidAiSqlException(
                    "La consulta SQL generada está vacía"
            );
        }

        String normalized =
                sql.trim().toUpperCase(Locale.ROOT);

        if (!normalized.startsWith("SELECT")
                && !normalized.startsWith("WITH")) {

            throw new InvalidAiSqlException(
                    "Solo se permiten consultas SELECT"
            );
        }

        if (SQL_COMMENT.matcher(sql).find()) {
            throw new InvalidAiSqlException(
                    "No se permiten comentarios SQL"
            );
        }

        validateSingleStatement(sql);

        /*
         * Eliminamos los strings antes de buscar palabras peligrosas.
         *
         * Así esto es válido:
         *
         * WHERE accion = 'DELETE'
         *
         * pero esto sigue bloqueado:
         *
         * DELETE FROM ITEM
         */
        String sqlWithoutStrings =
                removeStringLiterals(sql);

        if (FORBIDDEN_KEYWORDS
                .matcher(sqlWithoutStrings)
                .find()) {

            throw new InvalidAiSqlException(
                    "La consulta contiene una operación SQL no permitida"
            );
        }
    }

    private void validateSingleStatement(String sql) {

        String normalized = sql.trim();

        if (normalized.endsWith(";")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        if (normalized.contains(";")) {
            throw new InvalidAiSqlException(
                    "Solo se permite una consulta SQL por solicitud"
            );
        }
    }

    private String removeStringLiterals(String sql) {

        return sql.replaceAll(
                "'(?:''|[^'])*'",
                "''"
        );
    }
}