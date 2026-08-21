package co.santiago.ai.project;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee la documentación en Markdown del proyecto (README, guías,
 * enunciado original) para que la IA tenga contexto de negocio
 * más allá de lo que se puede inferir del esquema de la base de datos.
 */
@Service
public class DocsScanner {

    private static final List<Path> DOC_PATHS = List.of(
            Path.of("README.md"),
            Path.of("INSTALACIONCHATBOT.md"),
            Path.of("src/main/resources/Excercise.md")
    );

    public Map<String, String> readDocs() {

        Map<String, String> docs = new LinkedHashMap<>();

        for (Path path : DOC_PATHS) {

            if (!Files.isRegularFile(path)) {
                continue;
            }

            try {
                docs.put(
                        path.getFileName().toString(),
                        Files.readString(path)
                );
            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Error leyendo " + path, e
                );
            }
        }

        return docs;
    }
}
