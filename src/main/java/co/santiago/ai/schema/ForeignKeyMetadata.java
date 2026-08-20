package co.santiago.ai.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForeignKeyMetadata {

    private String column;
    private String referencedTable;
    private String referencedColumn;
}