package co.santiago.ai.project;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProjectIndexer {

    private final ProjectDocumentGenerator projectDocumentGenerator;
    private final VectorStore vectorStore;

    public ProjectIndexer(
            ProjectDocumentGenerator projectDocumentGenerator,
            VectorStore vectorStore
    ) {
        this.projectDocumentGenerator = projectDocumentGenerator;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void indexProject() {

        List<String> projectDocuments =
                projectDocumentGenerator.generateDocuments();

        List<Document> documents = projectDocuments
                .stream()
                .map(content ->
                        new Document(
                                content,
                                Map.of(
                                        "type",
                                        "PROJECT_KNOWLEDGE"
                                )
                        )
                )
                .toList();

        vectorStore.add(documents);

        log.info(
                "{} documentos de proyecto indexados en VectorStore",
                documents.size()
        );
    }
}
