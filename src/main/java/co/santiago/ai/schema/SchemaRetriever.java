package co.santiago.ai.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SchemaRetriever {

    private final VectorStore vectorStore;
    private final SchemaRelationExpander relationExpander;

    public SchemaRetriever(
            VectorStore vectorStore,
            SchemaRelationExpander relationExpander
    ) {
        this.vectorStore = vectorStore;
        this.relationExpander = relationExpander;
    }

    public List<String> searchRelevantSchema(String query) {

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .topK(6)
                        .filterExpression(
                                "type == 'DATABASE_SCHEMA'"
                        )
                        .build();

        List<Document> documents =
                vectorStore.similaritySearch(
                        searchRequest
                );

        log.info(
                "RAG encontró {} documentos para '{}'",
                documents.size(),
                query
        );

        List<String> retrievedSchema =
                documents.stream()
                        .map(Document::getText)
                        .toList();

        List<String> expandedSchema =
                relationExpander.expand(
                        retrievedSchema
                );

        log.info(
                "Schema expandido a {} tablas",
                expandedSchema.size()
        );

        return expandedSchema;
    }
}