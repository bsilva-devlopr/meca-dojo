package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.BeltResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.GraduationEventResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.StaffSummary;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.UserSummary;
import br.com.mecadojo.api.graduation.domain.Belt;
import br.com.mecadojo.api.graduation.domain.StudentGraduation;

final class GraduationMapper {
    private GraduationMapper() {
    }

    static BeltResponse belt(Belt belt) {
        return new BeltResponse(belt.getId(), belt.getCode(), belt.getDisplayName(), belt.getSystem(), belt.getSequence(), belt.getMaxDegree());
    }

    static GraduationEventResponse event(StudentGraduation graduation) {
        StaffSummary awardedBy = graduation.getAwardedByStaff() == null
            ? null
            : new StaffSummary(graduation.getAwardedByStaff().getId(), graduation.getAwardedByStaff().getDisplayName());
        UserSummary createdBy = new UserSummary(
            graduation.getCreatedByUser().getId(),
            graduation.getCreatedByUser().getName(),
            graduation.getCreatedByUser().getEmail()
        );
        return new GraduationEventResponse(
            graduation.getId(),
            belt(graduation.getBelt()),
            graduation.getDegree(),
            graduation.getGraduatedAt(),
            awardedBy,
            graduation.getType(),
            graduation.getNotes(),
            createdBy,
            graduation.getCorrectsGraduation() == null ? null : graduation.getCorrectsGraduation().getId(),
            graduation.getCorrectionReason(),
            graduation.getCreatedAt()
        );
    }
}
