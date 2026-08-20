package co.santiago.services;

import co.santiago.ai.schema.SchemaRetriever;
import co.santiago.ai.sql.SqlExecutionResult;
import co.santiago.services.SqlExecutionService;
import co.santiago.dto.ChatRequestDTO;
import co.santiago.dto.ChatResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final SchemaRetriever schemaRetriever;
    private final SqlExecutionService sqlExecutionService;

    public ChatServiceImpl(
            ChatClient.Builder builder,
            SchemaRetriever schemaRetriever,
            SqlExecutionService sqlExecutionService
    ) {
        this.chatClient = builder.build();
        this.schemaRetriever = schemaRetriever;
        this.sqlExecutionService = sqlExecutionService;
    }

    @Override
    public ChatResponseDTO ask(ChatRequestDTO request) {

        // 1. Recuperar dinámicamente el schema relevante
        List<String> relevantSchema =
                schemaRetriever.searchRelevantSchema(
                        request.getPregunta()
                );

        String schemaContext =
                String.join("\n\n", relevantSchema);

        log.info(
                "Schema recuperado para '{}': {}",
                request.getPregunta(),
                schemaContext
        );

        // 2. Generar SQL inicial
        String sql = chatClient
                .prompt()
                .system("""
                        Eres un experto generando consultas SQL para H2.

                        Tu responsabilidad es transformar la pregunta del usuario
                        en una consulta SQL utilizando exclusivamente el esquema
                        proporcionado.

                        ESQUEMA RELEVANTE:

                        %s

                        REGLAS:

                        - Genera únicamente consultas SELECT.
                        - Nunca generes INSERT, UPDATE, DELETE, DROP, ALTER,
                          CREATE, TRUNCATE o MERGE.
                        - Utiliza únicamente las tablas y columnas presentes
                          en el esquema proporcionado.
                        - Nunca inventes tablas o columnas.
                        - Respeta exactamente los valores de los ENUM definidos
                          en el esquema.
                        - Si necesitas relacionar tablas, utiliza las FOREIGN KEYS
                          presentes en el esquema.
                        - La base de datos es H2.
                        - No uses Markdown.
                        - No uses ```sql.
                        - No expliques la consulta.
                        - Devuelve exclusivamente la consulta SQL.
                        """.formatted(schemaContext))
                .user(request.getPregunta())
                .call()
                .content();

        sql = cleanSql(sql);

        log.info(
                "SQL inicial generado por Ollama: {}",
                sql
        );

        // 3. Validar, ejecutar y autocorregir si falla
        SqlExecutionResult executionResult =
                sqlExecutionService.execute(
                        request.getPregunta(),
                        schemaContext,
                        sql
                );

        List<Map<String, Object>> resultado =
                executionResult.data();

        log.info(
                "SQL final utilizado: {}",
                executionResult.sql()
        );

        log.info(
                "Resultado de base de datos: {}",
                resultado
        );

        // 4. Transformar resultado en lenguaje natural
        String respuesta = chatClient
                .prompt()
                .system("""
                        Eres un asistente de negocio.

                        Responde la pregunta del usuario utilizando
                        exclusivamente los resultados obtenidos de la
                        base de datos.

                        REGLAS:

                        - No inventes datos.
                        - Si no hay resultados, indícalo claramente.
                        - Expresa los valores monetarios de forma legible.
                        - Responde de forma clara y concisa.
                        """)
                .user("""
                        Pregunta:

                        %s

                        Resultado de la base de datos:

                        %s
                        """.formatted(
                        request.getPregunta(),
                        resultado
                ))
                .call()
                .content();

        return new ChatResponseDTO(respuesta);
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