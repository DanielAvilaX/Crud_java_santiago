package co.santiago.ai.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnMetadata {

    private String name;
    private String type;
    private boolean nullable;
}