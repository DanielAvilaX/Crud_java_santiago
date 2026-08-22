package co.santiago.services;

import co.santiago.ai.context.EnrichedContext;
import co.santiago.ai.context.EnrichedContextRetriever;
import co.santiago.ai.sql.SqlValidator;
import co.santiago.dto.ChatResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataQueryServiceImpl implements DataQueryService {

    private static final int MAX_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final EnrichedContextRetriever enrichedContextRetriever;
    private final SqlValidator sqlValidator;
    private final DatabaseQueryService databaseQueryService;

    public DataQueryServiceImpl(
            ChatClient.Builder builder,
            EnrichedContextRetriever enrichedContextRetriever,
            SqlValidator sqlValidator,
            DatabaseQueryService databaseQueryService
    ) {
        this.chatClient = builder.build();
        this.enrichedContextRetriever = enrichedContextRetriever;
        this.sqlValidator = sqlValidator;
        this.databaseQueryService = databaseQueryService;
    }

    @Override
    public ChatResponseDTO ask(String question) {

        long totalStart = System.currentTimeMillis();

        log.info("[data:start] Pregunta: '{}'", question);

        // =========================
        // 1. CONTEXTO ENRIQUECIDO (RAG de esquema + RAG de proyecto)
        // =========================

        long contextStart = System.currentTimeMillis();

        EnrichedContext context =
                enrichedContextRetriever.retrieve(question);

        log.info(
                "[data:context] recuperado en {} ms",
                System.currentTimeMillis() - contextStart
        );

        log.info(
                "[data:context-full]\n{}",
                context.fullContext()
        );

        // =========================
        // 2. GENERAR + EJECUTAR SQL
        // =========================

        String previousSql = null;
        String previousError = null;

        List<Map<String, Object>> result = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            log.info(
                    "[data:sql-attempt] intento {}/{}",
                    attempt,
                    MAX_ATTEMPTS
            );

            long generationStart =
                    System.currentTimeMillis();

            String sql = generateSql(
                    question,
                    context,
                    previousSql,
                    previousError
            );

            sql = cleanSql(sql);

            log.info(
                    "[data:sql-generated] intento={} tiempo={} ms sql='{}'",
                    attempt,
                    System.currentTimeMillis() - generationStart,
                    sql
            );

            try {

                long validationStart =
                        System.currentTimeMillis();

                sqlValidator.validate(sql);

                log.info(
                        "[data:sql-validation] OK en {} ms",
                        System.currentTimeMillis()
                                - validationStart
                );

                long executionStart =
                        System.currentTimeMillis();

                result =
                        databaseQueryService.executeSelect(sql);

                log.info(
                        "[data:sql-execution] OK en {} ms resultado={}",
                        System.currentTimeMillis()
                                - executionStart,
                        result
                );

                break;

            } catch (Exception e) {

                previousSql = sql;
                previousError = e.getMessage();

                log.warn(
                        "[data:sql-error] intento {}/{} sql='{}' error='{}'",
                        attempt,
                        MAX_ATTEMPTS,
                        sql,
                        e.getMessage()
                );

                if (attempt == MAX_ATTEMPTS) {
                    throw new RuntimeException(
                            "No fue posible generar una consulta SQL válida",
                            e
                    );
                }
            }
        }

        // =========================
        // 3. RESPUESTA FINAL
        // =========================

        long responseStart =
                System.currentTimeMillis();

        String answer = chatClient
                .prompt()
                .system("""
                        Eres un asistente de negocio.

                        Debes responder utilizando exclusivamente
                        el resultado REAL obtenido de la base de datos.

                        REGLAS:

                        - Nunca inventes datos.
                        - Nunca estimes resultados.
                        - Nunca asumas valores.
                        - Si el resultado es 0, responde 0.
                        - Si no existen registros, indícalo claramente.
                        - No muestres SQL salvo que el usuario lo pida.
                        - Responde en español.
                        - Sé claro y conciso.
                        """)
                .user("""
                        Pregunta:

                        %s

                        Resultado REAL de la base de datos:

                        %s
                        """.formatted(
                        question,
                        result
                ))
                .call()
                .content();

        log.info(
                "[data:answer] generada en {} ms",
                System.currentTimeMillis() - responseStart
        );

        log.info(
                "[data:end] request completo en {} ms",
                System.currentTimeMillis() - totalStart
        );

        return new ChatResponseDTO(answer);
    }

    private String generateSql(
            String question,
            EnrichedContext context,
            String previousSql,
            String previousError
    ) {

        String retryContext = "";

        if (previousError != null) {

            retryContext = """
            ATENCIÓN: la consulta anterior falló.

            SQL anterior:
            %s

            ERROR REAL DE LA BASE DE DATOS:
            %s

            Debes generar una consulta diferente y corregida.

            Reglas adicionales para este reintento:

            - No repitas una tabla o columna que el error indique que no existe.
            - Revisa DATABASE SCHEMA nuevamente.
            - Utiliza exclusivamente nombres presentes literalmente
              en DATABASE SCHEMA.
            - Respeta exactamente los valores ENUM del esquema.
            """.formatted(
                    previousSql,
                    previousError
            );
        }

        return chatClient
                .prompt()
                .system("""
                        Eres un experto generando SQL para H2.

                        %s

                        %s

                        REGLAS:

                        - Genera únicamente SELECT o WITH de lectura.
                        - Utiliza exclusivamente tablas y columnas
                          presentes en DATABASE SCHEMA.
                        - Nunca inventes tablas.
                        - Nunca inventes columnas.
                        - Respeta exactamente los ENUM.
                        - Utiliza PROJECT KNOWLEDGE para interpretar
                          conceptos del negocio.
                        - Si el proyecto indica soft delete,
                          respeta esa semántica.
                        - Si hay conflicto entre tu conocimiento general
                          y el contexto proporcionado, gana el contexto.
                        - No expliques nada.
                        - No uses Markdown.
                        - Devuelve exclusivamente SQL válido.
                        """.formatted(
                        context.fullContext(),
                        retryContext
                ))
                .user(question)
                .call()
                .content();
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
