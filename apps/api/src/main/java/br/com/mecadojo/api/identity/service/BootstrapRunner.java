package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.repository.RoleRepository;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
@EnableConfigurationProperties(BootstrapProperties.class)
public class BootstrapRunner implements CommandLineRunner {
    private final BootstrapProperties properties;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapRunner(
        BootstrapProperties properties,
        TenantRepository tenantRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.enabled()) {
            return;
        }
        if (properties.ownerPassword() == null || properties.ownerPassword().isBlank()) {
            throw new IllegalStateException("BOOTSTRAP_OWNER_PASSWORD is required when bootstrap is enabled");
        }

        String tenantSlug = SlugNormalizer.slug(properties.tenantSlug());
        String ownerEmail = SlugNormalizer.email(properties.ownerEmail());
        Tenant tenant = tenantRepository.findBySlug(tenantSlug)
            .orElseGet(() -> tenantRepository.save(new Tenant(properties.tenantName().trim(), tenantSlug)));

        if (userRepository.findByTenantIdAndEmail(tenant.getId(), ownerEmail).isEmpty()) {
            User owner = new User(
                tenant,
                properties.ownerName().trim(),
                ownerEmail,
                passwordEncoder.encode(properties.ownerPassword()),
                Set.of(roleRepository.findByName(RoleName.OWNER).orElseThrow())
            );
            userRepository.save(owner);
        }
    }
}
