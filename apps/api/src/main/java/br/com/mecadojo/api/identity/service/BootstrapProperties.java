package br.com.mecadojo.api.identity.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(
    boolean enabled,
    String tenantName,
    String tenantSlug,
    String ownerName,
    String ownerEmail,
    String ownerPassword
) {
}
