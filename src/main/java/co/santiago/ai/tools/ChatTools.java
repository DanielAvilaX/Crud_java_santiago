package co.santiago.ai.tools;

import co.santiago.ai.schema.SchemaRetriever;
import co.santiago.ai.project.ProjectRetriever;
import co.santiago.ai.sql.SqlValidator;
import co.santiago.services.DatabaseQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Herramientas que el modelo puede invocar de forma autónoma
 * (Fase 4 - Orquestación automática) en vez de que el código
 * fuerce siempre la misma secuencia fija de pasos.
 */
@Slf4j
@Component
public class ChatTools {

    private final SchemaRetriever schemaRetriever;
    private final ProjectRetriever projectRetriever;
    private final DatabaseQueryService databaseQueryService;
    private final SqlValidator sqlValidator;

    public ChatTools(
            SchemaRetriever schemaRetriever,
            ProjectRetriever projectRetriever,
            DatabaseQueryService databaseQueryService,
            SqlValidator sqlValidator
    ) {
        this.schemaRetriever = schemaRetriever;
        this.projectRetriever = projectRetriever;
        this.databaseQueryService = databaseQueryService;
        this.sqlValidator = sqlValidator;
    }

    @Tool(description = """
            Busca el esquema (tablas, columnas, primary keys y foreign keys)
            relevante en la base de datos H2 para responder una pregunta de negocio.
            Úsala antes de escribir cualquier consulta SQL.
            """)
    public String searchDatabaseSchema(
            @ToolParam(description = "La pregunta o tema a buscar en el esquema de la base de datos")
            String query
    ) {

        log.info("[tool] searchDatabaseSchema('{}')", query);

        return String.join(
                "\n\n",
                schemaRetriever.searchRelevantSchema(query)
        );
    }

    @Tool(description = """
            Busca conocimiento del proyecto: qué significan los estados (enums),
            qué campos tiene cada entidad de negocio, y documentación del proyecto.
            Úsala para entender el vocabulario de negocio antes de responder.
            """)
    public String searchProjectKnowledge(
            @ToolParam(description = "La pregunta o tema a buscar en el conocimiento del proyecto")
            String query
    ) {

        log.info("[tool] searchProjectKnowledge('{}')", query);

        return String.join(
                "\n\n",
                projectRetriever.searchRelevantProjectKnowledge(query)
        );
    }

    @Tool(description = """
            Ejecuta una consulta SQL de solo lectura (SELECT) contra la base
            de datos y devuelve el resultado. Solo se permiten SELECT.
            Si la consulta falla, corrige el SQL y vuelve a intentar.
            """)
    public String executeReadOnlyQuery(
            @ToolParam(description = "La consulta SQL SELECT a ejecutar")
            String sql
    ) {

        log.info("[tool] executeReadOnlyQuery('{}')", sql);

        try {

            sqlValidator.validate(sql);

            return databaseQueryService
                    .executeSelect(sql)
                    .toString();

        } catch (Exception e) {

            log.warn("[tool] executeReadOnlyQuery falló: {}", e.getMessage());

            return "ERROR ejecutando la consulta: " + e.getMessage();
        }
    }
}
