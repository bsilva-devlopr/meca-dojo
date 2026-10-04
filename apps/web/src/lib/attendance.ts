import { type UnitSummary, type UserSummary } from "@/src/lib/people/types";
import { type ClassModality } from "@/src/lib/schedule/types";

export type AttendanceSource = "QR" | "MANUAL" | "RECEPTION";
export type AttendanceStatus = "ACTIVE" | "VOIDED";
export type CheckInStatus = "ACCEPTED" | "DUPLICATE" | "EXPIRED" | "INVALID" | "SESSION_CLOSED" | "STUDENT_INVALID" | "UNLINKED_USER";

export type AttendanceStudentSummary = {
  id: string;
  fullName: string;
  preferredName: string | null;
  status: string;
};

export type AttendanceResponse = {
  id: string;
  trainingSessionId: string;
  student: AttendanceStudentSummary;
  source: AttendanceSource;
  status: AttendanceStatus;
  checkedInAt: string;
  recordedBy: UserSummary | null;
  checkInId: string | null;
  voidedAt: string | null;
  voidedBy: UserSummary | null;
  voidReason: string | null;
};

export type TrainingSessionSummary = {
  id: string;
  dojoClass: { id: string; name: string; modality: ClassModality };
  unit: UnitSummary;
  sessionDate: string;
  startTime: string;
  endTime: string;
  status: string;
};

export type CheckInTokenResponse = {
  token: string;
  expiresAt: string;
  checkInUrl: string;
};

export type SelfCheckInResponse = {
  status: CheckInStatus;
  message: string;
  attendance: AttendanceResponse | null;
  trainingSession: TrainingSessionSummary | null;
  occurredAt: string;
};

export type StudentAttendanceHistoryItem = {
  attendanceId: string;
  trainingSession: TrainingSessionSummary;
  source: AttendanceSource;
  checkedInAt: string;
};

export type StudentAttendanceSummary = {
  totalTrainings: number;
  last30Days: number;
  last90Days: number;
  giCount: number;
  noGiCount: number;
  currentMonth: number;
};

export const attendanceSourceLabels: Record<AttendanceSource, string> = {
  QR: "QR",
  MANUAL: "Manual",
  RECEPTION: "Recepcao",
};

