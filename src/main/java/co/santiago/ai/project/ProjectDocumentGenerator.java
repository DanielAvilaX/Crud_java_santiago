package co.santiago.ai.project;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProjectDocumentGenerator {

    private final JavaModelScanner javaModelScanner;
    private final DocsScanner docsScanner;

    public ProjectDocumentGenerator(
            JavaModelScanner javaModelScanner,
            DocsScanner docsScanner
    ) {
        this.javaModelScanner = javaModelScanner;
        this.docsScanner = docsScanner;
    }

    public List<String> generateDocuments() {

        List<String> documents = new ArrayList<>();

        for (CodeUnit unit : javaModelScanner.scanEnums()) {
            documents.add(generateCodeUnitDocument(unit));
        }

        for (CodeUnit unit : javaModelScanner.scanModels()) {
            documents.add(generateCodeUnitDocument(unit));
        }

        for (Map.Entry<String, String> doc :
                docsScanner.readDocs().entrySet()) {

            documents.addAll(chunkDocument(doc.getKey(), doc.getValue()));
        }

        return documents;
    }

    /**
     * Documentos largos como un solo bloque distorsionan la búsqueda
     * semántica (su embedding termina siendo un promedio de muchos
     * temas y opaca a documentos chicos y precisos como los enums).
     * Se parte por párrafos, acumulando hasta un tamaño máximo por
     * chunk, en vez de embeder el archivo completo de una sola vez.
     */
    private List<String> chunkDocument(String fileName, String content) {

        int maxChunkSize = 800;

        List<String> chunks = new ArrayList<>();
        String[] paragraphs = content
                .replace("\r\n", "\n")
                .split("\n{2,}");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {

            if (currentChunk.length() + paragraph.length() > maxChunkSize
                    && !currentChunk.isEmpty()) {

                chunks.add(buildChunk(fileName, currentChunk.toString()));
                currentChunk = new StringBuilder();
            }

            currentChunk.append(paragraph).append("\n\n");
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(buildChunk(fileName, currentChunk.toString()));
        }

        return chunks;
    }

    private String buildChunk(String fileName, String content) {
        return "DOCUMENTO: " + fileName + "\n\n" + content.trim();
    }

    private String generateCodeUnitDocument(CodeUnit unit) {

        StringBuilder document = new StringBuilder();

        document.append(unit.getKind())
                .append(": ")
                .append(unit.getPackageName())
                .append(".")
                .append(unit.getTypeName())
                .append("\n\n");

        if (!unit.getEnumConstants().isEmpty()) {

            document.append(
                    "Representa los posibles estados o valores de este campo.\n\n"
            );

            document.append("ESTADOS / VALORES POSIBLES:\n");

            for (String constant : unit.getEnumConstants()) {
                document.append("- ")
                        .append(constant)
                        .append("\n");
            }
        }

        if (!unit.getFields().isEmpty()) {

            document.append("CAMPOS:\n");

            for (String field : unit.getFields()) {
                document.append("- ")
                        .append(field)
                        .append("\n");
            }
        }

        return document.toString();
    }
}
