package br.com.mecadojo.api.identity.repository;

import br.com.mecadojo.api.identity.domain.Permission;
import br.com.mecadojo.api.identity.domain.PermissionName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(PermissionName name);
}
