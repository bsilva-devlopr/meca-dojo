package br.com.mecadojo.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DatabaseUrlEnvironmentPostProcessorTests {
    @Test
    void convertsManagedPostgresUrlToJdbcProperties() {
        Map<String, Object> properties = DatabaseUrlEnvironmentPostProcessor.fromEnvironment(Map.of(
            "DATABASE_URL", "postgresql://user%40dojo:secret%21@db.example.com:5432/meca?sslmode=require"
        ));

        assertThat(properties)
            .containsEntry("spring.datasource.url", "jdbc:postgresql://db.example.com:5432/meca?sslmode=require")
            .containsEntry("spring.datasource.username", "user@dojo")
            .containsEntry("spring.datasource.password", "secret!");
    }

    @Test
    void explicitDatabaseCredentialsOverrideUrlUserInfo() {
        Map<String, Object> properties = DatabaseUrlEnvironmentPostProcessor.fromEnvironment(Map.of(
            "DATABASE_URL", "postgres://url-user:url-password@db.example.com/meca",
            "DATABASE_USERNAME", "env-user",
            "DATABASE_PASSWORD", "env-password"
        ));

        assertThat(properties)
            .containsEntry("spring.datasource.url", "jdbc:postgresql://db.example.com/meca")
            .containsEntry("spring.datasource.username", "env-user")
            .containsEntry("spring.datasource.password", "env-password");
    }

    @Test
    void acceptsJdbcDatabaseUrlDirectly() {
        Map<String, Object> properties = DatabaseUrlEnvironmentPostProcessor.fromEnvironment(Map.of(
            "JDBC_DATABASE_URL", "jdbc:postgresql://db.internal:5432/meca"
        ));

        assertThat(properties).containsEntry("spring.datasource.url", "jdbc:postgresql://db.internal:5432/meca");
    }
}
