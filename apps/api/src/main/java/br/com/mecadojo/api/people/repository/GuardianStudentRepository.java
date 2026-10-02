package br.com.mecadojo.api.people.repository;

import br.com.mecadojo.api.people.domain.GuardianStudent;
import br.com.mecadojo.api.people.domain.GuardianStudentId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianStudentRepository extends JpaRepository<GuardianStudent, GuardianStudentId> {
    @EntityGraph(attributePaths = {"guardian", "student"})
    List<GuardianStudent> findAllByStudentId(UUID studentId);

    Optional<GuardianStudent> findByGuardianIdAndStudentId(UUID guardianId, UUID studentId);
}
