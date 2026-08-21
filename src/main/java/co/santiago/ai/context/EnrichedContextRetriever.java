package co.santiago.ai.context;

import co.santiago.ai.project.ProjectRetriever;
import co.santiago.ai.schema.SchemaRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EnrichedContextRetriever {

    private final SchemaRetriever schemaRetriever;
    private final ProjectRetriever projectRetriever;

    public EnrichedContextRetriever(
            SchemaRetriever schemaRetriever,
            ProjectRetriever projectRetriever
    ) {
        this.schemaRetriever = schemaRetriever;
        this.projectRetriever = projectRetriever;
    }

    public EnrichedContext retrieve(String question) {

        List<String> schemaDocuments =
                schemaRetriever.searchRelevantSchema(question);

        List<String> projectDocuments =
                projectRetriever.searchRelevantProjectKnowledge(question);

        String schemaContext =
                String.join(
                        "\n\n",
                        schemaDocuments
                );

        String projectContext =
                String.join(
                        "\n\n",
                        projectDocuments
                );

        log.info(
                "Contexto enriquecido recuperado para '{}': {} documentos schema",
                question,
                schemaDocuments.size()
        );

        log.info(
                "Contexto enriquecido recuperado para '{}': {} documentos proyecto",
                question,
                projectDocuments.size()
        );

        return new EnrichedContext(
                schemaContext,
                projectContext
        );
    }
}