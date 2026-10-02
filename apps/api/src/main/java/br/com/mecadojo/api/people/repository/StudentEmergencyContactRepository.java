package br.com.mecadojo.api.people.repository;

import br.com.mecadojo.api.people.domain.StudentEmergencyContact;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentEmergencyContactRepository extends JpaRepository<StudentEmergencyContact, UUID> {
    Optional<StudentEmergencyContact> findByStudentId(UUID studentId);

    void deleteByStudentId(UUID studentId);
}
