package br.com.mecadojo.api.attendance.domain;

public enum CheckInStatus {
    ACCEPTED,
    DUPLICATE,
    EXPIRED,
    INVALID,
    SESSION_CLOSED,
    STUDENT_INVALID,
    UNLINKED_USER
}
