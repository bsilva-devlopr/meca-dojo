package br.com.mecadojo.api.graduation.repository;

import br.com.mecadojo.api.graduation.domain.Belt;
import br.com.mecadojo.api.graduation.domain.BeltSystem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeltRepository extends JpaRepository<Belt, UUID> {
    List<Belt> findAllByActiveTrueOrderBySystemAscSequenceAsc();
    List<Belt> findAllBySystemAndActiveTrueOrderBySequenceAsc(BeltSystem system);
    List<Belt> findAllByCodeAndActiveTrueOrderBySystemAscSequenceAsc(String code);
    Optional<Belt> findBySystemAndCodeAndActiveTrue(BeltSystem system, String code);
    Optional<Belt> findBySystemAndSequenceAndActiveTrue(BeltSystem system, int sequence);
}
