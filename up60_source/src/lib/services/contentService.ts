import { isSupabaseConfigured, supabase } from "../supabase";
import type { SubjectFile, SubjectSection } from "./types";

// In-memory fallback content store for demo mode
let fallbackSections: SubjectSection[] = [
  {
    id: "sec-01",
    subject_id: "y1s2-03", // أسس الهندسة الكهربائية
    title: "المحاضرة 1: مقدمة في الدارات وقوانين كيرشوف",
    position: 0,
    files: [
      {
        id: "file-01",
        section_id: "sec-01",
        display_name: "ملخص المحاضرة الأولى.pdf",
        storage_path: "y1s2-03/sec-01/lecture1.pdf",
        position: 0,
      },
    ],
  },
  {
    id: "sec-02",
    subject_id: "y1s2-03",
    title: "المحاضرة 2: المكثفات والملفات في دارات التيار المستمر",
    position: 1,
    files: [
      {
        id: "file-02",
        section_id: "sec-02",
        display_name: "تمارين وتطبيقات عملية.pdf",
        storage_path: "y1s2-03/sec-02/exercises.pdf",
        position: 0,
      },
    ],
  },
];

export const contentService = {
  async getSectionsWithFiles(subjectId: string): Promise<SubjectSection[]> {
    if (!isSupabaseConfigured) {
      return fallbackSections
        .filter((sec) => sec.subject_id === subjectId)
        .sort((a, b) => a.position - b.position);
    }

    const { data: sectionsData, error: secError } = await supabase
      .from("subject_sections")
      .select("*")
      .eq("subject_id", subjectId)
      .order("position", { ascending: true });

    if (secError || !sectionsData) {
      return [];
    }

    const sectionIds = sectionsData.map((s) => s.id);
    if (sectionIds.length === 0) {
      return [];
    }

    const { data: filesData } = await supabase
      .from("subject_files")
      .select("*")
      .in("section_id", sectionIds)
      .order("position", { ascending: true });

    const filesBySection: Record<string, SubjectFile[]> = {};
    (filesData || []).forEach((f) => {
      if (!filesBySection[f.section_id]) {
        filesBySection[f.section_id] = [];
      }
      filesBySection[f.section_id]!.push(f as SubjectFile);
    });

    return sectionsData.map((sec) => ({
      ...sec,
      files: filesBySection[sec.id] || [],
    })) as SubjectSection[];
  },

  async createSection(subjectId: string, title: string, position: number): Promise<SubjectSection> {
    if (!isSupabaseConfigured) {
      const newSec: SubjectSection = {
        id: `sec-${Date.now()}`,
        subject_id: subjectId,
        title,
        position,
        files: [],
      };
      fallbackSections.push(newSec);
      return newSec;
    }

    const { data, error } = await supabase
      .from("subject_sections")
      .insert({
        subject_id: subjectId,
        title,
        position,
      })
      .select()
      .single();

    if (error || !data) {
      throw new Error(`فشل إنشاء العنوان: ${error?.message}`);
    }

    return { ...data, files: [] } as SubjectSection;
  },

  async updateSection(sectionId: string, title: string, position?: number): Promise<void> {
    if (!isSupabaseConfigured) {
      fallbackSections = fallbackSections.map((s) =>
        s.id === sectionId
          ? { ...s, title, position: position ?? s.position }
          : s
      );
      return;
    }

    const updatePayload: Record<string, unknown> = { title, updated_at: new Date().toISOString() };
    if (position !== undefined) updatePayload["position"] = position;

    const { error } = await supabase
      .from("subject_sections")
      .update(updatePayload)
      .eq("id", sectionId);

    if (error) {
      throw new Error(`فشل تحديث العنوان: ${error.message}`);
    }
  },

  async deleteSection(sectionId: string): Promise<void> {
    if (!isSupabaseConfigured) {
      fallbackSections = fallbackSections.filter((s) => s.id !== sectionId);
      return;
    }

    // Supabase foreign key on delete cascade will automatically clean up public.subject_files
    const { error } = await supabase
      .from("subject_sections")
      .delete()
      .eq("id", sectionId);

    if (error) {
      throw new Error(`فشل حذف العنوان: ${error.message}`);
    }
  },

  async createFile(sectionId: string, displayName: string, storagePath: string, position: number): Promise<SubjectFile> {
    if (!isSupabaseConfigured) {
      const newFile: SubjectFile = {
        id: `file-${Date.now()}`,
        section_id: sectionId,
        display_name: displayName,
        storage_path: storagePath,
        position,
      };

      fallbackSections = fallbackSections.map((sec) => {
        if (sec.id === sectionId) {
          return { ...sec, files: [...(sec.files || []), newFile] };
        }
        return sec;
      });

      return newFile;
    }

    const { data, error } = await supabase
      .from("subject_files")
      .insert({
        section_id: sectionId,
        display_name: displayName,
        storage_path: storagePath,
        position,
      })
      .select()
      .single();

    if (error || !data) {
      throw new Error(`فشل تسجيل ملف الـ PDF: ${error?.message}`);
    }

    return data as SubjectFile;
  },

  async updateFile(fileId: string, displayName: string, position?: number): Promise<void> {
    if (!isSupabaseConfigured) {
      fallbackSections = fallbackSections.map((sec) => ({
        ...sec,
        files: (sec.files || []).map((f) =>
          f.id === fileId ? { ...f, display_name: displayName, position: position ?? f.position } : f
        ),
      }));
      return;
    }

    const payload: Record<string, unknown> = { display_name: displayName, updated_at: new Date().toISOString() };
    if (position !== undefined) payload["position"] = position;

    const { error } = await supabase
      .from("subject_files")
      .update(payload)
      .eq("id", fileId);

    if (error) {
      throw new Error(`فشل تحديث بيانات الملف: ${error.message}`);
    }
  },

  async deleteFile(fileId: string): Promise<void> {
    if (!isSupabaseConfigured) {
      fallbackSections = fallbackSections.map((sec) => ({
        ...sec,
        files: (sec.files || []).filter((f) => f.id !== fileId),
      }));
      return;
    }

    const { error } = await supabase
      .from("subject_files")
      .delete()
      .eq("id", fileId);

    if (error) {
      throw new Error(`فشل حذف الملف: ${error.message}`);
    }
  },
};
