import { isSupabaseConfigured, supabase } from "../supabase";

const BUCKET_NAME = "subject-files";

export const storageService = {
  async uploadSubjectPdf(file: File, subjectId: string, sectionId: string): Promise<string> {
    const safeName = file.name.replace(/[^a-zA-Z0-9._-]/g, "_");
    const storagePath = `${subjectId}/${sectionId}/${Date.now()}_${safeName}`;

    if (!isSupabaseConfigured) {
      // Offline fallback: save blob URL or path in memory
      return storagePath;
    }

    const { error } = await supabase.storage
      .from(BUCKET_NAME)
      .upload(storagePath, file, {
        cacheControl: "3600",
        upsert: false,
      });

    if (error) {
      throw new Error(`فشل رفع ملف الـ PDF إلى التخزين الخاص: ${error.message}`);
    }

    return storagePath;
  },

  async getPdfSignedUrl(storagePath: string): Promise<string> {
    if (!isSupabaseConfigured) {
      // In offline mode, return a reliable sample PDF link
      return "https://raw.githubusercontent.com/mozilla/pdf.js/ba2edeae/web/compressed.tracemonkey-pldi-09.pdf";
    }

    const { data, error } = await supabase.storage
      .from(BUCKET_NAME)
      .createSignedUrl(storagePath, 3600); // 1 hour access

    if (error || !data?.signedUrl) {
      throw new Error(`تعذر توليد رابط الوصول للملف: ${error?.message || "رابط غير متاح"}`);
    }

    return data.signedUrl;
  },

  async deleteSubjectPdf(storagePath: string): Promise<void> {
    if (!isSupabaseConfigured) return;

    await supabase.storage.from(BUCKET_NAME).remove([storagePath]);
  },
};
