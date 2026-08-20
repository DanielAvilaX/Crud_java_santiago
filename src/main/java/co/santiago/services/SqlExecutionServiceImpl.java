package co.santiago.ai.sql;

import co.santiago.exceptions.AiSqlExecutionException;
import co.santiago.exceptions.InvalidAiSqlException;
import co.santiago.services.DatabaseQueryService;
import co.santiago.services.SqlExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SqlExecutionServiceImpl
        implements SqlExecutionService {

    private static final int MAX_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final DatabaseQueryService databaseQueryService;
    private final SqlValidator sqlValidator;

    public SqlExecutionServiceImpl(
            ChatClient.Builder builder,
            DatabaseQueryService databaseQueryService,
            SqlValidator sqlValidator
    ) {
        this.chatClient = builder.build();
        this.databaseQueryService =
                databaseQueryService;
        this.sqlValidator = sqlValidator;
    }

    @Override
    public SqlExecutionResult execute(
            String question,
            String schemaContext,
            String initialSql
    ) {

        String currentSql =
                cleanSql(initialSql);

        RuntimeException lastException = null;

        for (int attempt = 1;
             attempt <= MAX_ATTEMPTS;
             attempt++) {

            try {

                log.info(
                        "Intento SQL {}/{}: {}",
                        attempt,
                        MAX_ATTEMPTS,
                        currentSql
                );

                sqlValidator.validate(currentSql);

                List<Map<String, Object>> result =
                        databaseQueryService
                                .executeSelect(currentSql);

                log.info(
                        "SQL ejecutado correctamente en intento {}",
                        attempt
                );

                return new SqlExecutionResult(
                        currentSql,
                        result
                );

            } catch (
                    InvalidAiSqlException |
                    DataAccessException e
            ) {

                lastException = e;

                log.warn(
                        "SQL falló en intento {}: {}",
                        attempt,
                        e.getMessage()
                );

                if (attempt == MAX_ATTEMPTS) {
                    break;
                }

                currentSql = correctSql(
                        question,
                        schemaContext,
                        currentSql,
                        e.getMessage()
                );
            }
        }

        throw new AiSqlExecutionException(
                "No fue posible generar una consulta SQL válida después de "
                        + MAX_ATTEMPTS
                        + " intentos",
                lastException
        );
    }

    private String correctSql(
            String question,
            String schemaContext,
            String failedSql,
            String error
    ) {

        String correctedSql = chatClient
                .prompt()
                .system("""
                        Eres un experto corrigiendo consultas SQL para H2.

                        La consulta anterior falló.

                        Debes corregirla utilizando únicamente
                        el esquema proporcionado.

                        REGLAS:

                        - Genera únicamente SELECT.
                        - No inventes tablas.
                        - No inventes columnas.
                        - Respeta exactamente los ENUM.
                        - Usa las FOREIGN KEYS proporcionadas
                          para relacionar tablas.
                        - No uses Markdown.
                        - No uses ```sql.
                        - No expliques tu respuesta.
                        - Devuelve exclusivamente SQL válido.

                        ESQUEMA:

                        %s
                        """.formatted(schemaContext))
                .user("""
                        Pregunta original:

                        %s

                        SQL que falló:

                        %s

                        Error producido:

                        %s

                        Corrige la consulta.
                        """.formatted(
                        question,
                        failedSql,
                        error
                ))
                .call()
                .content();

        correctedSql =
                cleanSql(correctedSql);

        log.info(
                "SQL corregido por Ollama: {}",
                correctedSql
        );

        return correctedSql;
    }

    private String cleanSql(String sql) {

        if (sql == null) {
            return "";
        }

        return sql
                .replace("```sql", "")
                .replace("```SQL", "")
                .replace("```", "")
                .trim();
    }
}