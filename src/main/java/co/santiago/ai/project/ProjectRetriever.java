package co.santiago.ai.project;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProjectRetriever {

    private final VectorStore vectorStore;

    public ProjectRetriever(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<String> searchRelevantProjectKnowledge(String query) {

        // topK más alto que en SchemaRetriever a propósito: el corpus
        // de conocimiento de proyecto es chico (enums + entidades + docs),
        // así que traer más candidatos es barato y mejora el recall
        // (detectado con RagEvaluationTest).
        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .topK(6)
                        .filterExpression(
                                "type == 'PROJECT_KNOWLEDGE'"
                        )
                        .build();

        List<Document> documents =
                vectorStore.similaritySearch(
                        searchRequest
                );

        log.info(
                "RAG de proyecto encontró {} documentos para '{}'",
                documents.size(),
                query
        );

        return documents.stream()
                .map(Document::getText)
                .toList();
    }
}
