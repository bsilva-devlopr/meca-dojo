package br.com.mecadojo.api.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    static final String PROPERTY_SOURCE_NAME = "mecaDojoDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = fromEnvironment(System.getenv());
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    static Map<String, Object> fromEnvironment(Map<String, String> env) {
        Map<String, Object> properties = new LinkedHashMap<>();
        String jdbcDatabaseUrl = blankToNull(env.get("JDBC_DATABASE_URL"));
        String databaseUrl = blankToNull(env.get("DATABASE_URL"));

        if (jdbcDatabaseUrl != null) {
            properties.put("spring.datasource.url", jdbcDatabaseUrl);
        } else if (databaseUrl != null) {
            properties.putAll(parseDatabaseUrl(databaseUrl));
        }

        String username = blankToNull(env.get("DATABASE_USERNAME"));
        String password = blankToNull(env.get("DATABASE_PASSWORD"));
        if (username != null) {
            properties.put("spring.datasource.username", username);
        }
        if (password != null) {
            properties.put("spring.datasource.password", password);
        }
        return properties;
    }

    private static Map<String, Object> parseDatabaseUrl(String databaseUrl) {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            properties.put("spring.datasource.url", databaseUrl);
            return properties;
        }
        if (!databaseUrl.startsWith("postgres://") && !databaseUrl.startsWith("postgresql://")) {
            return properties;
        }

        URI uri = URI.create(databaseUrl);
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        int port = uri.getPort();
        String portPart = port == -1 ? "" : ":" + port;
        properties.put("spring.datasource.url", "jdbc:postgresql://" + uri.getHost() + portPart + uri.getRawPath() + query);

        String userInfo = uri.getRawUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            properties.put("spring.datasource.username", decode(parts[0]));
            if (parts.length > 1) {
                properties.put("spring.datasource.password", decode(parts[1]));
            }
        }
        return properties;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
