package br.com.mecadojo.api.people.service;

import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.domain.UserStatus;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UnitRepository;
import br.com.mecadojo.api.identity.repository.UserRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PeopleReferenceService {
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    public PeopleReferenceService(TenantRepository tenantRepository, UnitRepository unitRepository, UserRepository userRepository) {
        this.tenantRepository = tenantRepository;
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
    }

    public Tenant currentTenant() {
        return tenantRepository.findById(CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Tenant not found"));
    }

    public Unit unit(UUID unitId) {
        return unitRepository.findByIdAndTenantId(unitId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Unit not found"));
    }

    public User user(UUID userId) {
        return userRepository.findByIdAndTenantId(userId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User activeUser(UUID userId) {
        User user = user(userId);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new NotFoundException("User not found");
        }
        return user;
    }
}
