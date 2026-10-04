package br.com.mecadojo.api.attendance.repository;

import br.com.mecadojo.api.attendance.domain.CheckIn;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
}
