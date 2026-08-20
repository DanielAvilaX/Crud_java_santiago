package co.santiago.ai.schema;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableMetadata {

    private String name;

    private List<ColumnMetadata> columns = new ArrayList<>();

    private List<String> primaryKeys = new ArrayList<>();

    private List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();
}