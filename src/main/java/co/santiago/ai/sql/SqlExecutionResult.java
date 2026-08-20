package co.santiago.ai.sql;

import java.util.List;
import java.util.Map;

public record SqlExecutionResult(
        String sql,
        List<Map<String, Object>> data
) {
}