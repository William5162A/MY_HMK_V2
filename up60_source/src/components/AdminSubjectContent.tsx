import {
  AlertTriangle,
  ChevronDown,
  ChevronUp,
  Edit2,
  Eye,
  FilePlus,
  FileText,
  FolderPlus,
  Globe,
  GlobeLock,
  Plus,
  Trash2,
  UploadCloud,
  X,
} from "lucide-react";
import { useEffect, useState } from "react";
import { contentService } from "@/lib/services/contentService";
import { storageService } from "@/lib/services/storageService";
import type { SubjectFile, SubjectSection } from "@/lib/services/types";
import { PdfViewerModal } from "./PdfViewerModal";

interface AdminSubjectContentProps {
  subjectId: string;
  subjectName: string;
  isPublished: boolean;
  onTogglePublish: () => Promise<void>;
}

export function AdminSubjectContent({
  subjectId,
  subjectName,
  isPublished,
  onTogglePublish,
}: AdminSubjectContentProps) {
  const [activeTab, setActiveTab] = useState<"content" | "exams" | "practice">("content");
  const [sections, setSections] = useState<SubjectSection[]>([]);
  const [loading, setLoading] = useState(true);
  const [publishing, setPublishing] = useState(false);

  // Modals state
  const [showAddSectionModal, setShowAddSectionModal] = useState(false);
  const [newSectionTitle, setNewSectionTitle] = useState("");
  const [editingSection, setEditingSection] = useState<SubjectSection | null>(null);
  const [deletingSectionId, setDeletingSectionId] = useState<string | null>(null);

  // File Upload modal state
  const [uploadSectionId, setUploadSectionId] = useState<string | null>(null);
  const [fileDisplayName, setFileDisplayName] = useState("");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [editingFile, setEditingFile] = useState<SubjectFile | null>(null);

  // PDF Preview modal state
  const [previewPdf, setPreviewPdf] = useState<{ title: string; url: string } | null>(null);

  const loadSections = async () => {
    try {
      setLoading(true);
      const data = await contentService.getSectionsWithFiles(subjectId);
      setSections(data);
    } catch (err) {
      console.error("Error loading sections:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSections();
  }, [subjectId]);

  const handleTogglePublish = async () => {
    try {
      setPublishing(true);
      await onTogglePublish();
    } finally {
      setPublishing(false);
    }
  };

  // Section Handlers
  const handleCreateSection = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSectionTitle.trim()) return;

    try {
      await contentService.createSection(subjectId, newSectionTitle.trim(), sections.length);
      setNewSectionTitle("");
      setShowAddSectionModal(false);
      await loadSections();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`خطأ: ${message}`);
    }
  };

  const handleUpdateSection = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingSection || !editingSection.title.trim()) return;

    try {
      await contentService.updateSection(editingSection.id, editingSection.title.trim());
      setEditingSection(null);
      await loadSections();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`خطأ: ${message}`);
    }
  };

  const handleDeleteSection = async (sectionId: string) => {
    try {
      // Find files under this section and clean up storage
      const sec = sections.find((s) => s.id === sectionId);
      if (sec?.files) {
        for (const file of sec.files) {
          await storageService.deleteSubjectPdf(file.storage_path);
        }
      }

      await contentService.deleteSection(sectionId);
      setDeletingSectionId(null);
      await loadSections();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`خطأ في الحذف: ${message}`);
    }
  };

  const handleMoveSection = async (index: number, direction: "up" | "down") => {
    const targetIndex = direction === "up" ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= sections.length) return;

    const current = sections[index];
    const target = sections[targetIndex];
    if (!current || !target) return;

    try {
      await Promise.all([
        contentService.updateSection(current.id, current.title, target.position),
        contentService.updateSection(target.id, target.title, current.position),
      ]);
      await loadSections();
    } catch (err) {
      console.error(err);
    }
  };

  // File Handlers
  const handleUploadFile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!uploadSectionId || !selectedFile || !fileDisplayName.trim()) return;

    try {
      setUploading(true);
      const storagePath = await storageService.uploadSubjectPdf(selectedFile, subjectId, uploadSectionId);
      const currentSec = sections.find((s) => s.id === uploadSectionId);
      const pos = currentSec?.files?.length || 0;

      await contentService.createFile(uploadSectionId, fileDisplayName.trim(), storagePath, pos);

      setUploadSectionId(null);
      setSelectedFile(null);
      setFileDisplayName("");
      await loadSections();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`خطأ في الرفع: ${message}`);
    } finally {
      setUploading(false);
    }
  };

  const handlePreviewPdf = async (file: SubjectFile) => {
    try {
      const signedUrl = await storageService.getPdfSignedUrl(file.storage_path);
      setPreviewPdf({ title: file.display_name, url: signedUrl });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`تعذر فتح الملف: ${message}`);
    }
  };

  const handleDeleteFile = async (file: SubjectFile) => {
    if (!confirm(`هل أنت متأكد من حذف الملف "${file.display_name}"؟`)) return;

    try {
      await storageService.deleteSubjectPdf(file.storage_path);
      await contentService.deleteFile(file.id);
      await loadSections();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`خطأ: ${message}`);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Admin Controls Banner */}
      <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-accent/30 bg-accent/5 p-4 sm:p-5">
        <div className="flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-xl bg-accent text-accent-foreground font-bold">
            🛡️
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-foreground">لوحة تحكم المشرف — إدارة محتوى المادة</span>
              <span
                className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${
                  isPublished
                    ? "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30"
                    : "bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/30"
                }`}
              >
                {isPublished ? "منشورة للطلاب" : "مسودة (غير منشورة)"}
              </span>
            </div>
            <p className="text-xs text-muted-foreground mt-0.5">
              {isPublished
                ? "المادة متاحة حالياً لجميع الطلاب في فهرس المواد الدراسية."
                : "المادة محجوبة عن الطلاب ولا تظهر في الفهرس حتى يتم نشرها."}
            </p>
          </div>
        </div>

        {/* Publish / Unpublish Action Button */}
        <button
          type="button"
          onClick={handleTogglePublish}
          disabled={publishing}
          className={`inline-flex items-center gap-2 rounded-xl px-5 py-2.5 text-sm font-bold shadow-sm transition ${
            isPublished
              ? "bg-amber-600 hover:bg-amber-700 text-white"
              : "bg-emerald-600 hover:bg-emerald-700 text-white"
          }`}
        >
          {isPublished ? <GlobeLock className="h-4 w-4" /> : <Globe className="h-4 w-4" />}
          <span>{publishing ? "جاري التحديث..." : isPublished ? "إلغاء النشر (تحويل لمسودة)" : "نشر المادة للطلاب"}</span>
        </button>
      </div>

      {/* 3 Content Tabs (In Exact Specified Order) */}
      <div role="tablist" aria-label="أقسام إدارة المحتوى" className="grid grid-cols-3 border-b border-border text-center">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "content"}
          onClick={() => setActiveTab("content")}
          className={`border-b-2 py-3.5 text-sm font-bold transition ${
            activeTab === "content"
              ? "border-accent text-accent"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          1. شرح المادة
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "exams"}
          onClick={() => setActiveTab("exams")}
          className={`border-b-2 py-3.5 text-sm font-bold transition ${
            activeTab === "exams"
              ? "border-accent text-accent"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          2. أسئلة الدورات
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "practice"}
          onClick={() => setActiveTab("practice")}
          className={`border-b-2 py-3.5 text-sm font-bold transition ${
            activeTab === "practice"
              ? "border-accent text-accent"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          3. الأسئلة التي لم تأتِ في الدورات
        </button>
      </div>

      {/* TAB 1: شرح المادة (Headings & PDF Files CRUD) */}
      {activeTab === "content" && (
        <div className="space-y-6">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h3 className="text-lg font-bold text-foreground">العناوين والمحاضرات (Headings)</h3>
              <p className="text-xs text-muted-foreground">قم بإضافة العناوين الرئيسية ورفع ملفات الـ PDF الخاصة بكل عنوان.</p>
            </div>

            <button
              type="button"
              onClick={() => setShowAddSectionModal(true)}
              className="inline-flex items-center gap-2 rounded-xl bg-accent px-4 py-2.5 text-sm font-bold text-accent-foreground shadow-soft hover:brightness-105"
            >
              <Plus className="h-4 w-4" />
              <span>إضافة عنوان جديد</span>
            </button>
          </div>

          {loading ? (
            <div className="p-12 text-center text-muted-foreground">جاري تحميل العناوين والمحتوى...</div>
          ) : sections.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-border p-10 text-center">
              <FolderPlus className="mx-auto h-12 w-12 text-muted-foreground/40 mb-3" />
              <p className="font-bold text-foreground">لا توجد عناوين مضافة لهذه المادة بعد</p>
              <p className="mt-1 text-xs text-muted-foreground">انقر على زر "إضافة عنوان جديد" لإنشاء عنوانك الأول ورفع ملفات الـ PDF.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {sections.map((section, idx) => (
                <div
                  key={section.id}
                  className="rounded-2xl border border-border bg-card shadow-soft overflow-hidden transition"
                >
                  {/* Section Heading Bar */}
                  <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border/70 bg-muted/30 px-4 py-3 sm:px-5">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <span className="grid h-7 w-7 shrink-0 place-items-center rounded-lg bg-accent/15 text-accent text-xs font-bold">
                        {idx + 1}
                      </span>
                      <h4 className="truncate font-bold text-foreground text-base">{section.title}</h4>
                      <span className="text-xs text-muted-foreground">({section.files?.length || 0} ملفات)</span>
                    </div>

                    <div className="flex items-center gap-1">
                      {/* Move Up / Down */}
                      <button
                        type="button"
                        onClick={() => handleMoveSection(idx, "up")}
                        disabled={idx === 0}
                        title="تحريك لأعلى"
                        className="p-1.5 text-muted-foreground hover:text-foreground disabled:opacity-30"
                      >
                        <ChevronUp className="h-4 w-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => handleMoveSection(idx, "down")}
                        disabled={idx === sections.length - 1}
                        title="تحريك لأسفل"
                        className="p-1.5 text-muted-foreground hover:text-foreground disabled:opacity-30"
                      >
                        <ChevronDown className="h-4 w-4" />
                      </button>

                      {/* Edit Title */}
                      <button
                        type="button"
                        onClick={() => setEditingSection(section)}
                        title="تعديل العنوان"
                        className="p-1.5 text-muted-foreground hover:text-foreground"
                      >
                        <Edit2 className="h-4 w-4" />
                      </button>

                      {/* Delete Section */}
                      <button
                        type="button"
                        onClick={() => setDeletingSectionId(section.id)}
                        title="حذف العنوان"
                        className="p-1.5 text-destructive/80 hover:text-destructive"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </div>

                  {/* Section Files List */}
                  <div className="p-4 sm:p-5 space-y-3">
                    {(!section.files || section.files.length === 0) ? (
                      <p className="text-xs text-muted-foreground italic">لا توجد ملفات PDF في هذا العنوان بعد.</p>
                    ) : (
                      <div className="grid gap-2 sm:grid-cols-2">
                        {section.files.map((file) => (
                          <div
                            key={file.id}
                            className="flex items-center justify-between gap-3 rounded-xl border border-border bg-background p-3"
                          >
                            <div className="flex items-center gap-2.5 min-w-0">
                              <span className="grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-red-500/10 text-red-500">
                                <FileText className="h-4 w-4" />
                              </span>
                              <span className="truncate text-sm font-semibold text-foreground">{file.display_name}</span>
                            </div>

                            <div className="flex items-center gap-1 shrink-0">
                              <button
                                type="button"
                                onClick={() => handlePreviewPdf(file)}
                                title="معاينة الملف"
                                className="grid h-8 w-8 place-items-center rounded-lg bg-accent/10 text-accent hover:bg-accent/20"
                              >
                                <Eye className="h-4 w-4" />
                              </button>
                              <button
                                type="button"
                                onClick={() => handleDeleteFile(file)}
                                title="حذف الملف"
                                className="grid h-8 w-8 place-items-center rounded-lg text-destructive/70 hover:bg-destructive/10 hover:text-destructive"
                              >
                                <Trash2 className="h-4 w-4" />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Add PDF button for this section */}
                    <button
                      type="button"
                      onClick={() => {
                        setUploadSectionId(section.id);
                        setFileDisplayName("");
                        setSelectedFile(null);
                      }}
                      className="inline-flex items-center gap-1.5 text-xs font-bold text-accent hover:underline mt-2"
                    >
                      <FilePlus className="h-4 w-4" />
                      <span>إضافة ملف PDF إلى هذا العنوان</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: أسئلة الدورات (Preserved with clean Arabic empty state) */}
      {activeTab === "exams" && (
        <div className="rounded-3xl border border-border bg-card p-10 text-center shadow-soft">
          <div className="mx-auto grid h-16 w-16 place-items-center rounded-2xl bg-accent/15 text-accent text-2xl font-bold mb-4">
            📝
          </div>
          <h3 className="font-display text-2xl font-bold text-foreground">أسئلة الدورات</h3>
          <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">
            لا توجد أسئلة دورات امتحانية مضافة لهذه المادة حالياً.
          </p>
          <div className="mt-6 inline-flex items-center gap-2 rounded-full border border-border bg-muted/40 px-4 py-2 text-xs font-semibold text-muted-foreground">
            <span>جاهزة لربط بنك الأسئلة والاختبارات التفاعلية مستقبلاً</span>
          </div>
        </div>
      )}

      {/* TAB 3: الأسئلة التي لم تأتِ في الدورات (Preserved with clean Arabic empty state) */}
      {activeTab === "practice" && (
        <div className="rounded-3xl border border-border bg-card p-10 text-center shadow-soft">
          <div className="mx-auto grid h-16 w-16 place-items-center rounded-2xl bg-accent/15 text-accent text-2xl font-bold mb-4">
            💡
          </div>
          <h3 className="font-display text-2xl font-bold text-foreground">الأسئلة التي لم تأتِ في الدورات</h3>
          <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">
            لا توجد أسئلة إثرائية إضافية مضافة لهذه المادة حالياً.
          </p>
          <div className="mt-6 inline-flex items-center gap-2 rounded-full border border-border bg-muted/40 px-4 py-2 text-xs font-semibold text-muted-foreground">
            <span>مساحة مخصصة للتدريبات الإضافية والأسئلة النموذجية</span>
          </div>
        </div>
      )}

      {/* MODAL: Create Heading */}
      {showAddSectionModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-border">
              <h4 className="font-bold text-lg text-foreground">إضافة عنوان جديد</h4>
              <button type="button" onClick={() => setShowAddSectionModal(false)} className="text-muted-foreground hover:text-foreground">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleCreateSection} className="mt-4 space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1.5 text-foreground">عنوان المحاضرة أو الفصل</label>
                <input
                  type="text"
                  value={newSectionTitle}
                  onChange={(e) => setNewSectionTitle(e.target.value)}
                  placeholder="مثال: المحاضرة 1: مدخل إلى المادة"
                  className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:border-accent"
                  required
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddSectionModal(false)}
                  className="rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground hover:bg-muted"
                >
                  إلغاء
                </button>
                <button
                  type="submit"
                  className="rounded-xl bg-accent px-5 py-2 text-sm font-bold text-accent-foreground hover:brightness-105"
                >
                  إضافة العنوان
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: Edit Section Title */}
      {editingSection && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-border">
              <h4 className="font-bold text-lg text-foreground">تعديل العنوان</h4>
              <button type="button" onClick={() => setEditingSection(null)} className="text-muted-foreground hover:text-foreground">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleUpdateSection} className="mt-4 space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1.5 text-foreground">عنوان المحاضرة</label>
                <input
                  type="text"
                  value={editingSection.title}
                  onChange={(e) => setEditingSection({ ...editingSection, title: e.target.value })}
                  className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:border-accent"
                  required
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setEditingSection(null)}
                  className="rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground hover:bg-muted"
                >
                  إلغاء
                </button>
                <button
                  type="submit"
                  className="rounded-xl bg-accent px-5 py-2 text-sm font-bold text-accent-foreground hover:brightness-105"
                >
                  حفظ التعديلات
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: Confirm Delete Section with Cleanup Notice */}
      {deletingSectionId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-2xl">
            <div className="flex items-center gap-3 text-destructive">
              <AlertTriangle className="h-6 w-6" />
              <h4 className="font-bold text-lg">تأكيد حذف العنوان</h4>
            </div>
            <p className="mt-3 text-sm text-muted-foreground leading-relaxed">
              هل أنت متأكد من رغبتك في حذف هذا العنوان؟ سيتم حذف جميع ملفات الـ PDF التابعة له نهائياً من قاعدة البيانات والتخزين السحابي لتجنب بقاء أي ملفات معلقة.
            </p>
            <div className="mt-6 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setDeletingSectionId(null)}
                className="rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground hover:bg-muted"
              >
                تراجع
              </button>
              <button
                type="button"
                onClick={() => handleDeleteSection(deletingSectionId)}
                className="rounded-xl bg-destructive px-5 py-2 text-sm font-bold text-destructive-foreground hover:brightness-110"
              >
                تأكيد الحذف
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: Upload PDF to Section */}
      {uploadSectionId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-border">
              <h4 className="font-bold text-lg text-foreground">إضافة ملف PDF جديد</h4>
              <button type="button" onClick={() => setUploadSectionId(null)} className="text-muted-foreground hover:text-foreground">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleUploadFile} className="mt-4 space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-1.5 text-foreground">اسم الملف المعروض للطالب</label>
                <input
                  type="text"
                  value={fileDisplayName}
                  onChange={(e) => setFileDisplayName(e.target.value)}
                  placeholder="مثال: ملخص المحاضرة الأولى.pdf"
                  className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:border-accent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-semibold mb-1.5 text-foreground">اختر ملف الـ PDF</label>
                <div className="relative border-2 border-dashed border-border rounded-xl p-4 text-center hover:border-accent transition">
                  <input
                    type="file"
                    accept=".pdf,application/pdf"
                    onChange={(e) => {
                      const f = e.target.files?.[0];
                      if (f) {
                        setSelectedFile(f);
                        if (!fileDisplayName) setFileDisplayName(f.name);
                      }
                    }}
                    className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    required
                  />
                  <UploadCloud className="mx-auto h-8 w-8 text-accent mb-1" />
                  <p className="text-xs font-semibold text-foreground">
                    {selectedFile ? selectedFile.name : "انقر لاختيار ملف PDF من جهازك"}
                  </p>
                  <p className="text-[10px] text-muted-foreground mt-0.5">الملف سيتم حفظه بأمان في Bucket خاص</p>
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setUploadSectionId(null)}
                  disabled={uploading}
                  className="rounded-xl border border-border px-4 py-2 text-sm font-semibold text-foreground hover:bg-muted"
                >
                  إلغاء
                </button>
                <button
                  type="submit"
                  disabled={uploading || !selectedFile}
                  className="rounded-xl bg-accent px-5 py-2 text-sm font-bold text-accent-foreground hover:brightness-105 disabled:opacity-50"
                >
                  {uploading ? "جاري الرفع..." : "رفع الملف"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* PDF Viewer Modal */}
      {previewPdf && (
        <PdfViewerModal
          title={previewPdf.title}
          url={previewPdf.url}
          isOpen={true}
          onClose={() => setPreviewPdf(null)}
        />
      )}
    </div>
  );
}
