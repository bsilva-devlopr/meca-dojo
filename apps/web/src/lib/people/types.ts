export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type UnitSummary = {
  id: string;
  name: string;
};

export type UserSummary = {
  id: string;
  name: string;
  email: string;
};

export type StudentListItem = {
  id: string;
  fullName: string;
  preferredName: string | null;
  status: StudentStatus;
  joinDate: string;
  primaryUnit: UnitSummary;
};

export type StudentStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED" | "TRIAL";

export type StudentResponse = StudentListItem & {
  birthDate: string | null;
  email: string | null;
  phone: string | null;
  user: UserSummary | null;
  emergencyContact: EmergencyContact | null;
  guardians: StudentGuardian[];
  notes: string | null;
};

export type EmergencyContact = {
  id: string;
  name: string;
  relationship: string;
  phone: string;
};

export type StudentGuardian = {
  id: string;
  fullName: string;
  email: string | null;
  phone: string;
  relationship: GuardianRelationship;
  primary: boolean;
};

export type GuardianRelationship = "FATHER" | "MOTHER" | "LEGAL_GUARDIAN" | "GRANDPARENT" | "OTHER";

export type StaffType = "PROFESSOR" | "INSTRUCTOR";
export type StaffStatus = "ACTIVE" | "INACTIVE";

export type StaffResponse = {
  id: string;
  type: StaffType;
  status: StaffStatus;
  displayName: string;
  user: UserSummary;
  primaryUnit: UnitSummary | null;
};
