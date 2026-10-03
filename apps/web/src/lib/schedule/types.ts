export type ClassModality = "GI" | "NO_GI" | "MIXED" | "OTHER";
export type ClassAudience = "ADULT" | "KIDS" | "TEENS" | "ALL";
export type ClassType = "REGULAR" | "FUNDAMENTALS" | "COMPETITION" | "OPEN_MAT" | "PRIVATE" | "OTHER";
export type ClassLevel = "ALL_LEVELS" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type ClassStatus = "ACTIVE" | "INACTIVE";
export type ScheduleStatus = "ACTIVE" | "INACTIVE";
export type DayOfWeek = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
export type ScheduleStaffRole = "LEAD" | "ASSISTANT";

export type DojoClass = {
  id: string;
  name: string;
  description: string | null;
  modality: ClassModality;
  audience: ClassAudience;
  classType: ClassType;
  level: ClassLevel;
  status: ClassStatus;
  defaultCapacity: number | null;
};

export type ScheduleStaff = {
  id: string;
  displayName: string;
  type: string;
  role: ScheduleStaffRole;
};

export type ClassSchedule = {
  id: string;
  dojoClass: DojoClass;
  unit: { id: string; name: string };
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  capacityOverride: number | null;
  effectiveCapacity: number | null;
  status: ScheduleStatus;
  staff: ScheduleStaff[];
};

export type WeeklySchedule = {
  days: { dayOfWeek: DayOfWeek; schedules: ClassSchedule[] }[];
};

export const orderedDays: DayOfWeek[] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
export const dayLabels: Record<DayOfWeek, string> = { MONDAY: "Segunda", TUESDAY: "Terca", WEDNESDAY: "Quarta", THURSDAY: "Quinta", FRIDAY: "Sexta", SATURDAY: "Sabado", SUNDAY: "Domingo" };
export const dayShortLabels: Record<DayOfWeek, string> = { MONDAY: "SEG", TUESDAY: "TER", WEDNESDAY: "QUA", THURSDAY: "QUI", FRIDAY: "SEX", SATURDAY: "SAB", SUNDAY: "DOM" };
export const modalityLabels: Record<ClassModality, string> = { GI: "Gi", NO_GI: "No-Gi", MIXED: "Mista", OTHER: "Outra" };
export const audienceLabels: Record<ClassAudience, string> = { ADULT: "Adulto", KIDS: "Kids", TEENS: "Teens", ALL: "Todos" };
export const classTypeLabels: Record<ClassType, string> = { REGULAR: "Regular", FUNDAMENTALS: "Fundamentos", COMPETITION: "Competicao", OPEN_MAT: "Open Mat", PRIVATE: "Privada", OTHER: "Outra" };
export const levelLabels: Record<ClassLevel, string> = { ALL_LEVELS: "Todos os niveis", BEGINNER: "Iniciante", INTERMEDIATE: "Intermediario", ADVANCED: "Avancado" };
