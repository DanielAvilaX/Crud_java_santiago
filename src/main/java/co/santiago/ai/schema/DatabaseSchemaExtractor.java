package co.santiago.ai.schema;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseSchemaExtractor {

    private final DataSource dataSource;

    // Schema almacenado en memoria
    private List<TableMetadata> cachedSchema;

    public DatabaseSchemaExtractor(
            @Qualifier("dataSource") DataSource dataSource
    ) {
        this.dataSource = dataSource;
    }

    /**
     * Devuelve el schema desde cache.
     * Si todavía no existe, lo carga desde la base de datos.
     */
    public synchronized List<TableMetadata> extractSchema() {

        if (cachedSchema == null) {
            cachedSchema = loadSchema();
        }

        return cachedSchema;
    }

    /**
     * Fuerza una nueva lectura de la metadata de la BD.
     */
    public synchronized List<TableMetadata> reloadSchema() {

        cachedSchema = loadSchema();

        return cachedSchema;
    }

    /**
     * Consulta realmente DatabaseMetaData.
     */
    private List<TableMetadata> loadSchema() {

        List<TableMetadata> tables = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata =
                    connection.getMetaData();

            try (ResultSet tableResult = metadata.getTables(
                    connection.getCatalog(),
                    "PUBLIC",
                    "%",
                    new String[]{"TABLE"}
            )) {

                while (tableResult.next()) {

                    String tableName =
                            tableResult.getString("TABLE_NAME");

                    String tableSchema =
                            tableResult.getString("TABLE_SCHEM");

                    if (!"PUBLIC".equalsIgnoreCase(tableSchema)) {
                        continue;
                    }

                    if (isSystemTable(tableName)) {
                        continue;
                    }

                    TableMetadata table =
                            new TableMetadata();

                    table.setName(tableName);

                    loadColumns(metadata, table);
                    loadPrimaryKeys(metadata, table);
                    loadForeignKeys(metadata, table);

                    tables.add(table);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error extrayendo esquema de base de datos",
                    e
            );
        }

        return List.copyOf(tables);
    }

    private void loadColumns(
            DatabaseMetaData metadata,
            TableMetadata table
    ) throws SQLException {

        try (ResultSet columns = metadata.getColumns(
                null,
                "PUBLIC",
                table.getName(),
                null
        )) {

            while (columns.next()) {

                String columnName =
                        columns.getString("COLUMN_NAME");

                String typeName =
                        columns.getString("TYPE_NAME");

                boolean nullable =
                        columns.getInt("NULLABLE")
                                == DatabaseMetaData.columnNullable;

                table.getColumns().add(
                        new ColumnMetadata(
                                columnName,
                                typeName,
                                nullable
                        )
                );
            }
        }
    }

    private void loadPrimaryKeys(
            DatabaseMetaData metadata,
            TableMetadata table
    ) throws SQLException {

        try (ResultSet primaryKeys =
                     metadata.getPrimaryKeys(
                             null,
                             "PUBLIC",
                             table.getName()
                     )) {

            while (primaryKeys.next()) {

                table.getPrimaryKeys().add(
                        primaryKeys.getString("COLUMN_NAME")
                );
            }
        }
    }

    private void loadForeignKeys(
            DatabaseMetaData metadata,
            TableMetadata table
    ) throws SQLException {

        try (ResultSet foreignKeys =
                     metadata.getImportedKeys(
                             null,
                             "PUBLIC",
                             table.getName()
                     )) {

            while (foreignKeys.next()) {

                table.getForeignKeys().add(
                        new ForeignKeyMetadata(
                                foreignKeys.getString("FKCOLUMN_NAME"),
                                foreignKeys.getString("PKTABLE_NAME"),
                                foreignKeys.getString("PKCOLUMN_NAME")
                        )
                );
            }
        }
    }

    /**
     * Busca una tabla concreta dentro del schema cacheado.
     */
    public TableMetadata findTable(String tableName) {

        return extractSchema()
                .stream()
                .filter(table ->
                        table.getName()
                                .equalsIgnoreCase(tableName)
                )
                .findFirst()
                .orElse(null);
    }
    public List<TableMetadata> findTablesReferencing(
            String tableName
    ) {

        return extractSchema()
                .stream()
                .filter(table ->
                        table.getForeignKeys()
                                .stream()
                                .anyMatch(fk ->
                                        fk.getReferencedTable()
                                                .equalsIgnoreCase(tableName)
                                )
                )
                .toList();
    }

    private boolean isSystemTable(String tableName) {

        String name =
                tableName.toUpperCase();

        return name.startsWith("SYS")
                || name.startsWith("INFORMATION_SCHEMA");
    }
}