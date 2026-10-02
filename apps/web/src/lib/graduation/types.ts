import { bffJson } from "@/src/lib/people/client";
import { type PageResponse, type StaffResponse } from "@/src/lib/people/types";

export type BeltSystem = "GENERAL" | "KIDS";
export type GraduationType = "INITIAL" | "BELT_PROMOTION" | "DEGREE_PROMOTION" | "CORRECTION";

export type BeltResponse = {
  id: string;
  code: string;
  name: string;
  system: BeltSystem;
  sequence: number;
  maxDegree: number;
};

export type GraduationStaffSummary = {
  id: string;
  name: string;
};

export type GraduationUserSummary = {
  id: string;
  name: string;
  email: string;
};

export type GraduationEvent = {
  id: string;
  belt: BeltResponse;
  degree: number;
  graduatedAt: string | null;
  awardedBy: GraduationStaffSummary | null;
  type: GraduationType;
  notes: string | null;
  createdBy: GraduationUserSummary;
  correctsGraduationId: string | null;
  correctionReason: string | null;
  createdAt: string;
};

export type Eligibility = {
  eligible: boolean;
  reasons: string[];
  warnings: string[];
};

export type CurrentGraduation = {
  current: GraduationEvent | null;
  eligibility: Eligibility;
};

export async function getBelts() {
  return bffJson<BeltResponse[]>("/belts");
}

export async function getCurrentGraduation(studentId: string) {
  return bffJson<CurrentGraduation>(`/students/${studentId}/graduation`);
}

export async function getGraduationHistory(studentId: string) {
  return bffJson<PageResponse<GraduationEvent>>(`/students/${studentId}/graduations?page=0&size=20`);
}

export async function getActiveGraduationStaff() {
  const [professors, instructors] = await Promise.all([
    bffJson<PageResponse<StaffResponse>>("/staff?type=PROFESSOR&status=ACTIVE&page=0&size=100").catch(() => null),
    bffJson<PageResponse<StaffResponse>>("/staff?type=INSTRUCTOR&status=ACTIVE&page=0&size=100").catch(() => null),
  ]);
  return [...(professors?.content ?? []), ...(instructors?.content ?? [])];
}
