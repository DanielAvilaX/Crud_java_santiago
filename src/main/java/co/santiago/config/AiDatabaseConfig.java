package co.santiago.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Conexión separada y de solo lectura para las consultas SQL
 * que genera la IA, distinta de la conexión administrativa
 * que usa el resto de la aplicación.
 *
 * Al definir aquí un segundo bean DataSource, Spring Boot deja de
 * crear automáticamente el principal, por eso también se redefine
 * explícitamente como @Primary con la misma configuración de
 * application.properties.
 */
@Configuration
public class AiDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(
            DataSourceProperties dataSourceProperties
    ) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "aiReadOnlyDataSource")
    public DataSource aiReadOnlyDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.driver-class-name}") String driverClassName,
            @Value("${ai.datasource.username}") String username,
            @Value("${ai.datasource.password}") String password
    ) {

        return DataSourceBuilder.create()
                .url(url)
                .driverClassName(driverClassName)
                .username(username)
                .password(password)
                .build();
    }

    @Bean(name = "aiJdbcTemplate")
    public JdbcTemplate aiJdbcTemplate(
            @Qualifier("aiReadOnlyDataSource") DataSource aiReadOnlyDataSource,
            @Value("${ai.sql.max-rows}") int maxRows,
            @Value("${ai.sql.query-timeout-seconds}") int queryTimeoutSeconds
    ) {

        JdbcTemplate jdbcTemplate =
                new JdbcTemplate(aiReadOnlyDataSource);

        jdbcTemplate.setMaxRows(maxRows);
        jdbcTemplate.setQueryTimeout(queryTimeoutSeconds);

        return jdbcTemplate;
    }
}
