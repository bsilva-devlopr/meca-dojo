package br.com.mecadojo.api.identity.repository;

import br.com.mecadojo.api.identity.domain.Role;
import br.com.mecadojo.api.identity.domain.RoleName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByName(RoleName name);

    @EntityGraph(attributePaths = "permissions")
    List<Role> findAllByNameIn(Collection<RoleName> names);
}
