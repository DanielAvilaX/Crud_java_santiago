package co.santiago.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
public class AiReadOnlyUserInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final String username;
    private final String password;

    public AiReadOnlyUserInitializer(
            @Qualifier("dataSource") DataSource dataSource,
            @Value("${ai.datasource.username}") String username,
            @Value("${ai.datasource.password}") String password
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {

        jdbcTemplate.execute(
                "CREATE USER IF NOT EXISTS " + username
                        + " PASSWORD '" + password + "'"
        );

        jdbcTemplate.execute(
                "GRANT SELECT ON SCHEMA PUBLIC TO " + username
        );

        log.info(
                "Usuario de solo lectura '{}' listo para consultas de IA",
                username
        );
    }
}
