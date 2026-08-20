package co.santiago.services;

import co.santiago.ai.sql.SqlExecutionResult;

public interface SqlExecutionService {

    SqlExecutionResult execute(
            String question,
            String schemaContext,
            String initialSql
    );
}