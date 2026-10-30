export type AppRole = "student" | "admin";

export interface Profile {
  id: string;
  display_name: string;
  academic_year: number;
  semester: number;
  created_at?: string;
  updated_at?: string;
}

export interface UserRoleRecord {
  id: string;
  user_id: string;
  role: AppRole;
  created_at?: string;
}

export interface SubjectRecord {
  id: string;
  name: string;
  academic_year: number;
  semester: number;
  is_published: boolean;
  created_at?: string;
  updated_at?: string;
}

export interface SubjectSection {
  id: string;
  subject_id: string;
  title: string;
  position: number;
  files?: SubjectFile[];
  created_at?: string;
  updated_at?: string;
}

export interface SubjectFile {
  id: string;
  section_id: string;
  display_name: string;
  storage_path: string;
  position: number;
  created_at?: string;
  updated_at?: string;
}

export interface PurchaseRecord {
  id: string;
  user_id: string;
  subject_id: string;
  status: string;
  created_at?: string;
}
