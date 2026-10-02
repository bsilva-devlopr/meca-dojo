package br.com.mecadojo.api.people.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GuardianStudentId implements Serializable {
    private UUID guardianId;
    private UUID studentId;

    protected GuardianStudentId() {
    }

    public GuardianStudentId(UUID guardianId, UUID studentId) {
        this.guardianId = guardianId;
        this.studentId = studentId;
    }

    public UUID getGuardianId() { return guardianId; }
    public UUID getStudentId() { return studentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuardianStudentId that)) return false;
        return Objects.equals(guardianId, that.guardianId) && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guardianId, studentId);
    }
}
