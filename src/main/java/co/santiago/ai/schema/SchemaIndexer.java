package co.santiago.ai.schema;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SchemaIndexer {

    private final SchemaDocumentGenerator schemaDocumentGenerator;
    private final VectorStore vectorStore;

    public SchemaIndexer(
            SchemaDocumentGenerator schemaDocumentGenerator,
            VectorStore vectorStore
    ) {
        this.schemaDocumentGenerator = schemaDocumentGenerator;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void indexSchema() {

        List<String> schemaDocuments =
                schemaDocumentGenerator.generateDocuments();

        List<Document> documents = schemaDocuments
                .stream()
                .map(content ->
                        new Document(
                                content,
                                Map.of(
                                        "type",
                                        "DATABASE_SCHEMA"
                                )
                        )
                )
                .toList();

        vectorStore.add(documents);

        log.info(
                "{} tablas indexadas en VectorStore",
                documents.size()
        );
    }
}