package co.santiago.ai.schema;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaDocumentGenerator {

    private final DatabaseSchemaExtractor schemaExtractor;

    public SchemaDocumentGenerator(
            DatabaseSchemaExtractor schemaExtractor
    ) {
        this.schemaExtractor = schemaExtractor;
    }

    public List<String> generateDocuments() {

        return schemaExtractor.extractSchema()
                .stream()
                .map(this::generateTableDocument)
                .toList();
    }

    public String generateTableDocument(TableMetadata table) {

        StringBuilder document = new StringBuilder();

        document.append("TABLE: ")
                .append(table.getName())
                .append("\n\n");

        document.append("COLUMNS:\n");

        for (ColumnMetadata column : table.getColumns()) {

            document.append("- ")
                    .append(column.getName())
                    .append(" ")
                    .append(column.getType());

            if (!column.isNullable()) {
                document.append(" NOT NULL");
            }

            document.append("\n");
        }

        if (!table.getPrimaryKeys().isEmpty()) {

            document.append("\nPRIMARY KEY:\n");

            for (String primaryKey : table.getPrimaryKeys()) {
                document.append("- ")
                        .append(primaryKey)
                        .append("\n");
            }
        }

        if (!table.getForeignKeys().isEmpty()) {

            document.append("\nFOREIGN KEYS:\n");

            for (ForeignKeyMetadata foreignKey :
                    table.getForeignKeys()) {

                document.append("- ")
                        .append(foreignKey.getColumn())
                        .append(" -> ")
                        .append(foreignKey.getReferencedTable())
                        .append(".")
                        .append(foreignKey.getReferencedColumn())
                        .append("\n");
            }
        }

        return document.toString();
    }
}