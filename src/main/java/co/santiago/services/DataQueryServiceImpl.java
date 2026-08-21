package co.santiago.services;

import co.santiago.ai.project.ProjectRetriever;
import co.santiago.ai.schema.SchemaRetriever;
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
    private final SchemaRetriever schemaRetriever;
    private final ProjectRetriever projectRetriever;
    private final SqlValidator sqlValidator;
    private final DatabaseQueryService databaseQueryService;

    public DataQueryServiceImpl(
            ChatClient.Builder builder,
            SchemaRetriever schemaRetriever,
            ProjectRetriever projectRetriever,
            SqlValidator sqlValidator,
            DatabaseQueryService databaseQueryService
    ) {
        this.chatClient = builder.build();
        this.schemaRetriever = schemaRetriever;
        this.projectRetriever = projectRetriever;
        this.sqlValidator = sqlValidator;
        this.databaseQueryService = databaseQueryService;
    }

    @Override
    public ChatResponseDTO ask(String question) {

        long totalStart = System.currentTimeMillis();

        log.info("[data:start] Pregunta: '{}'", question);

        // =========================
        // 1. SCHEMA RAG
        // =========================

        long schemaStart = System.currentTimeMillis();

        List<String> schemaDocuments =
                schemaRetriever.searchRelevantSchema(question);

        String schemaContext =
                String.join("\n\n", schemaDocuments);

        log.info(
                "[data:schema] recuperado en {} ms",
                System.currentTimeMillis() - schemaStart
        );

        log.info(
                "[data:schema-context]\n{}",
                schemaContext
        );

        // =========================
        // 2. PROJECT RAG
        // =========================

        long projectStart = System.currentTimeMillis();

        List<String> projectDocuments =
                projectRetriever.searchRelevantProjectKnowledge(question);

        String projectContext =
                String.join("\n\n", projectDocuments);

        log.info(
                "[data:project] recuperado en {} ms",
                System.currentTimeMillis() - projectStart
        );

        // =========================
        // 3. GENERAR + EJECUTAR SQL
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
                    schemaContext,
                    projectContext,
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
        // 4. RESPUESTA FINAL
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
            String schemaContext,
            String projectContext,
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

                        DATABASE SCHEMA:

                        %s

                        PROJECT KNOWLEDGE:

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
                        schemaContext,
                        projectContext,
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