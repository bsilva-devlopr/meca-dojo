package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.EligibilityResponse;
import br.com.mecadojo.api.graduation.domain.GraduationPolicy;
import br.com.mecadojo.api.graduation.domain.StudentGraduation;
import br.com.mecadojo.api.graduation.repository.GraduationPolicyRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class GraduationEligibilityService {
    private final GraduationPolicyRepository policyRepository;

    public GraduationEligibilityService(GraduationPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public EligibilityResponse evaluate(StudentGraduation current) {
        if (current == null) {
            return new EligibilityResponse(false, java.util.List.of("Graduacao atual nao cadastrada"), java.util.List.of());
        }
        var policy = policyRepository.findByTenantIdAndBeltId(CurrentUser.get().tenantId(), current.getBelt().getId())
            .filter(GraduationPolicy::isActive)
            .orElse(null);
        var reasons = new ArrayList<String>();
        var warnings = new ArrayList<String>();

        if (policy == null) {
            warnings.add("Nenhuma politica de elegibilidade configurada para a faixa atual");
            return new EligibilityResponse(true, reasons, warnings);
        }

        if (policy.getMinimumDaysInGrade() != null) {
            if (current.getGraduatedAt() == null) {
                reasons.add("Data da graduacao atual desconhecida");
            } else {
                long days = ChronoUnit.DAYS.between(current.getGraduatedAt(), LocalDate.now());
                if (days < policy.getMinimumDaysInGrade()) {
                    reasons.add("Tempo minimo na graduacao atual ainda nao atingido");
                }
            }
        }
        if (policy.getMinimumTrainingCount() != null) {
            warnings.add("Criterio de frequencia ainda nao disponivel neste ciclo");
        }
        return new EligibilityResponse(reasons.isEmpty(), reasons, warnings);
    }
}
