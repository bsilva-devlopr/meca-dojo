import { type DojoClass } from "@/src/lib/schedule/types";

export type TrainingSessionStatus = "SCHEDULED" | "OPEN" | "COMPLETED" | "CANCELLED";
export type TrainingSessionStaffRole = "LEAD" | "ASSISTANT";

export type TrainingSessionStaff = {
  id: string;
  displayName: string;
  type: string;
  role: TrainingSessionStaffRole;
};

export type TrainingSession = {
  id: string;
  dojoClass: DojoClass;
  classSchedule: { id: string } | null;
  unit: { id: string; name: string };
  sessionDate: string;
  startTime: string;
  endTime: string;
  status: TrainingSessionStatus;
  capacity: number | null;
  notes: string | null;
  staff: TrainingSessionStaff[];
  createdBy: { id: string; name: string; email: string };
  cancelledAt: string | null;
  cancelledBy: { id: string; name: string; email: string } | null;
  cancellationReason: string | null;
};

export const trainingStatusLabels: Record<TrainingSessionStatus, string> = {
  SCHEDULED: "Planejado",
  OPEN: "Em andamento",
  COMPLETED: "Concluido",
  CANCELLED: "Cancelado",
};

export const trainingStatusTone: Record<TrainingSessionStatus, "neutral" | "success" | "warning" | "danger" | "info" | "primary"> = {
  SCHEDULED: "info",
  OPEN: "success",
  COMPLETED: "neutral",
  CANCELLED: "danger",
};
