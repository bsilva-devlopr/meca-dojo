package br.com.mecadojo.api.attendance.repository;

import br.com.mecadojo.api.attendance.domain.CheckInToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInTokenRepository extends JpaRepository<CheckInToken, UUID> {
    @EntityGraph(attributePaths = {"tenant", "trainingSession", "trainingSession.dojoClass", "trainingSession.unit", "createdByUser"})
    Optional<CheckInToken> findByTokenHash(String tokenHash);
}
