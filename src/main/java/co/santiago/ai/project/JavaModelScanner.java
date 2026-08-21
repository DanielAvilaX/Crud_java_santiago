package co.santiago.ai.project;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae de forma ligera (sin parser de Java) los enums y las
 * entidades del proyecto, para que la IA conozca el vocabulario
 * de negocio: qué estados existen, qué campos tiene una factura, etc.
 *
 * Asume que la app corre desde la raíz del proyecto
 * (ej. "mvn spring-boot:run"), ya que lee los .java directamente
 * del código fuente en disco, no del classpath compilado.
 */
@Service
public class JavaModelScanner {

    private static final Path ENUMS_DIR =
            Path.of("src/main/java/co/santiago/enums");

    private static final Path MODELS_DIR =
            Path.of("src/main/java/co/santiago/models");

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("package\\s+([\\w.]+)\\s*;");

    private static final Pattern ENUM_PATTERN =
            Pattern.compile(
                    "public\\s+enum\\s+(\\w+)\\s*\\{([^}]*)}",
                    Pattern.DOTALL
            );

    private static final Pattern TYPE_NAME_PATTERN =
            Pattern.compile(
                    "public\\s+class\\s+(\\w+)"
            );

    private static final Pattern FIELD_PATTERN =
            Pattern.compile(
                    "private\\s+([\\w<>,\\s]+?)\\s+(\\w+)\\s*(=.*)?;"
            );

    public List<CodeUnit> scanEnums() {
        return scanDirectory(ENUMS_DIR, this::readEnum);
    }

    public List<CodeUnit> scanModels() {
        return scanDirectory(MODELS_DIR, this::readModel);
    }

    private List<CodeUnit> scanDirectory(
            Path directory,
            java.util.function.Function<Path, CodeUnit> reader
    ) {

        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        try (var files = Files.list(directory)) {

            List<CodeUnit> units = new ArrayList<>();

            for (Path file : files.toList()) {

                if (!file.toString().endsWith(".java")) {
                    continue;
                }

                CodeUnit unit = reader.apply(file);

                if (unit != null) {
                    units.add(unit);
                }
            }

            return units;

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Error leyendo " + directory, e
            );
        }
    }

    private CodeUnit readEnum(Path file) {

        String content = readFile(file);

        Matcher enumMatcher = ENUM_PATTERN.matcher(content);

        if (!enumMatcher.find()) {
            return null;
        }

        CodeUnit unit = new CodeUnit();

        unit.setKind("ENUM");
        unit.setPackageName(extractPackage(content));
        unit.setTypeName(enumMatcher.group(1));

        for (String constant : enumMatcher.group(2).split(",")) {

            String trimmed = constant.trim();

            if (!trimmed.isEmpty()) {
                unit.getEnumConstants().add(trimmed);
            }
        }

        return unit;
    }

    private CodeUnit readModel(Path file) {

        String content = readFile(file);

        Matcher typeMatcher = TYPE_NAME_PATTERN.matcher(content);

        if (!typeMatcher.find()) {
            return null;
        }

        CodeUnit unit = new CodeUnit();

        unit.setKind("ENTIDAD");
        unit.setPackageName(extractPackage(content));
        unit.setTypeName(typeMatcher.group(1));

        Matcher fieldMatcher = FIELD_PATTERN.matcher(content);

        while (fieldMatcher.find()) {

            String type = fieldMatcher.group(1).trim();
            String name = fieldMatcher.group(2).trim();

            unit.getFields().add(name + " (" + type + ")");
        }

        return unit;
    }

    private String extractPackage(String content) {

        Matcher matcher = PACKAGE_PATTERN.matcher(content);

        return matcher.find() ? matcher.group(1) : "";
    }

    private String readFile(Path file) {

        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Error leyendo " + file, e
            );
        }
    }
}
