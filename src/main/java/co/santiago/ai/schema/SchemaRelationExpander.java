package co.santiago.ai.schema;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SchemaRelationExpander {

    private final DatabaseSchemaExtractor schemaExtractor;
    private final SchemaDocumentGenerator documentGenerator;

    public SchemaRelationExpander(
            DatabaseSchemaExtractor schemaExtractor,
            SchemaDocumentGenerator documentGenerator
    ) {
        this.schemaExtractor = schemaExtractor;
        this.documentGenerator = documentGenerator;
    }

    public List<String> expand(List<String> retrievedDocuments) {

        List<TableMetadata> allTables =
                schemaExtractor.extractSchema();

        Set<String> tableNames =
                extractTableNames(retrievedDocuments);

        Set<String> expandedTableNames =
                new LinkedHashSet<>(tableNames);

        for (String tableName : tableNames) {

            TableMetadata table = findTable(
                    allTables,
                    tableName
            );

            if (table == null) {
                continue;
            }

            // Relaciones salientes:
            // LINE_ITEM -> INVOICE
            for (ForeignKeyMetadata fk : table.getForeignKeys()) {

                expandedTableNames.add(
                        fk.getReferencedTable()
                );
            }

            // Relaciones entrantes:
            // INVOICE <- LINE_ITEM
            // INVOICE <- PAYMENT
            for (TableMetadata candidate : allTables) {

                boolean referencesCurrentTable =
                        candidate.getForeignKeys()
                                .stream()
                                .anyMatch(fk ->
                                        fk.getReferencedTable()
                                                .equalsIgnoreCase(tableName)
                                );

                if (referencesCurrentTable) {
                    expandedTableNames.add(
                            candidate.getName()
                    );
                }
            }
        }

        return documentGenerator
                .generateDocuments()
                .stream()
                .filter(document ->
                        expandedTableNames.contains(
                                extractTableName(document)
                        )
                )
                .toList();
    }

    private Set<String> extractTableNames(
            List<String> documents
    ) {

        Set<String> tableNames =
                new LinkedHashSet<>();

        for (String document : documents) {

            String tableName =
                    extractTableName(document);

            if (tableName != null) {
                tableNames.add(tableName);
            }
        }

        return tableNames;
    }

    private String extractTableName(String document) {

        if (document == null ||
                !document.startsWith("TABLE: ")) {
            return null;
        }

        int end =
                document.indexOf("\n");

        if (end == -1) {
            return document
                    .substring("TABLE: ".length())
                    .trim();
        }

        return document
                .substring(
                        "TABLE: ".length(),
                        end
                )
                .trim();
    }

    private TableMetadata findTable(
            List<TableMetadata> tables,
            String tableName
    ) {

        return tables.stream()
                .filter(table ->
                        table.getName()
                                .equalsIgnoreCase(tableName)
                )
                .findFirst()
                .orElse(null);
    }
}