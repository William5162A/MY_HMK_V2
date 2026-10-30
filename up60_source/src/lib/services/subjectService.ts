import { SUBJECTS } from "../curriculum";
import { isSupabaseConfigured, supabase } from "../supabase";
import type { AppRole, SubjectRecord } from "./types";

// In-memory published state tracking for demo/fallback mode
const fallbackPublishedState: Record<string, boolean> = {
  // We can have 2 initial published subjects for demo if offline, otherwise all default to false
  "y1s2-03": true, // أسس الهندسة الكهربائية
  "y1s1-01": true, // الكيمياء الصناعية
};

export const subjectService = {
  async getSubjects(role: AppRole): Promise<SubjectRecord[]> {
    if (!isSupabaseConfigured) {
      // In fallback mode, map from curriculum.ts source of truth
      const all: SubjectRecord[] = SUBJECTS.map((s) => ({
        id: s.id,
        name: s.name,
        academic_year: s.year,
        semester: s.semester,
        is_published: fallbackPublishedState[s.id] ?? false,
      }));

      if (role === "admin") {
        return all;
      }
      return all.filter((s) => s.is_published);
    }

    // Live Supabase
    let query = supabase.from("subjects").select("*").order("academic_year", { ascending: true }).order("semester", { ascending: true });
    
    if (role === "student") {
      query = query.eq("is_published", true);
    }

    const { data, error } = await query;

    if (error || !data || data.length === 0) {
      // If table is not seeded yet, seed it and return
      await this.seedInitialSubjects();
      return this.getSubjects(role);
    }

    return data as SubjectRecord[];
  },

  async togglePublish(subjectId: string, currentStatus: boolean): Promise<boolean> {
    const newStatus = !currentStatus;

    if (!isSupabaseConfigured) {
      fallbackPublishedState[subjectId] = newStatus;
      return newStatus;
    }

    const { error } = await supabase
      .from("subjects")
      .update({ is_published: newStatus, updated_at: new Date().toISOString() })
      .eq("id", subjectId);

    if (error) {
      throw new Error(`فشل تحديث حالة نشر المادة: ${error.message}`);
    }

    return newStatus;
  },

  async seedInitialSubjects(): Promise<void> {
    if (!isSupabaseConfigured) return;

    const rows = SUBJECTS.map((s) => ({
      id: s.id,
      name: s.name,
      academic_year: s.year,
      semester: s.semester,
      is_published: false, // Initial state: all false
    }));

    await supabase.from("subjects").upsert(rows, { onConflict: "id" });
  },
};
