package co.santiago.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DatabaseQueryServiceImpl implements DatabaseQueryService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseQueryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> executeSelect(String sql) {

        validateQuery(sql);

        log.info("Ejecutando SQL: {}", sql);

        return jdbcTemplate.queryForList(sql);
    }

    private void validateQuery(String sql) {

        if (sql == null || sql.isBlank()) {
            throw new RuntimeException("Consulta SQL vacía");
        }

        String normalized = sql
                .trim()
                .toUpperCase();

        // Únicamente permitimos SELECT
        if (!sql.trim().toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException(
                    "Solo se permiten consultas SELECT"
            );
        }
    }
}