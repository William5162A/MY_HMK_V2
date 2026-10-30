import {
  CheckCircle2,
  Eye,
  FileText,
  Lock,
  ShoppingBag,
  Sparkles,
} from "lucide-react";
import { useEffect, useState } from "react";
import { contentService } from "@/lib/services/contentService";
import { storageService } from "@/lib/services/storageService";
import type { SubjectFile, SubjectSection } from "@/lib/services/types";
import { useStore } from "@/lib/store";
import { PdfViewerModal } from "./PdfViewerModal";

interface StudentSubjectContentProps {
  subjectId: string;
  subjectName: string;
  isOwned: boolean;
}

export function StudentSubjectContent({
  subjectId,
  subjectName,
  isOwned,
}: StudentSubjectContentProps) {
  const { purchase } = useStore();
  const [activeTab, setActiveTab] = useState<"content" | "exams" | "practice">("content");
  const [sections, setSections] = useState<SubjectSection[]>([]);
  const [loading, setLoading] = useState(false);

  // Purchase Modal State
  const [showPurchaseModal, setShowPurchaseModal] = useState(false);
  const [purchasing, setPurchasing] = useState(false);
  const [purchaseSuccess, setPurchaseSuccess] = useState(false);

  // PDF Viewer Modal
  const [previewPdf, setPreviewPdf] = useState<{ title: string; url: string } | null>(null);

  useEffect(() => {
    if (isOwned) {
      setLoading(true);
      contentService
        .getSectionsWithFiles(subjectId)
        .then(setSections)
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [subjectId, isOwned]);

  const handleConfirmPurchase = async () => {
    try {
      setPurchasing(true);
      await purchase(subjectId);
      setPurchaseSuccess(true);
      setTimeout(() => {
        setPurchaseSuccess(false);
        setShowPurchaseModal(false);
      }, 1400);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(message);
    } finally {
      setPurchasing(false);
    }
  };

  const handleOpenPdf = async (file: SubjectFile) => {
    try {
      const signedUrl = await storageService.getPdfSignedUrl(file.storage_path);
      setPreviewPdf({ title: file.display_name, url: signedUrl });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "خطأ غير معروف";
      alert(`تعذر فتح الملف: ${message}`);
    }
  };

  // If student has NOT purchased this published subject
  if (!isOwned) {
    return (
      <div className="rounded-3xl border border-border bg-card p-8 sm:p-12 text-center shadow-soft">
        <div className="mx-auto grid h-16 w-16 place-items-center rounded-2xl bg-accent/15 text-accent mb-4">
          <Lock className="h-8 w-8" />
        </div>
        <h3 className="font-display text-2xl font-bold text-foreground">محتوى المادة مقفل</h3>
        <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">
          هذه المادة منشورة وجاهزة للدراسة. يمكنك إضافتها لموادك المشتراة لفتح جميع المحاضرات وملفات الـ PDF.
        </p>

        <button
          type="button"
          onClick={() => setShowPurchaseModal(true)}
          className="mt-6 inline-flex items-center gap-2 rounded-xl bg-accent px-8 py-3.5 text-base font-bold text-accent-foreground shadow-lift hover:brightness-105 transition"
        >
          <ShoppingBag className="h-5 w-5" />
          <span>شراء المادة</span>
        </button>

        {/* Purchase Confirmation Modal */}
        {showPurchaseModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-2xl text-right">
              {purchaseSuccess ? (
                <div className="py-6 text-center space-y-3">
                  <CheckCircle2 className="mx-auto h-14 w-14 text-emerald-500 animate-bounce" />
                  <h4 className="font-display text-xl font-bold text-foreground">تمت إضافة المادة بنجاح!</h4>
                  <p className="text-sm text-muted-foreground">تمت إضافة المادة إلى موادك المشتراة بنجاح.</p>
                </div>
              ) : (
                <>
                  <div className="flex items-center gap-2.5 pb-3 border-b border-border">
                    <Sparkles className="h-5 w-5 text-accent" />
                    <h4 className="font-bold text-lg text-foreground">شراء المادة</h4>
                  </div>

                  <p className="mt-4 text-base font-medium text-foreground leading-relaxed">
                    هل أنت متأكد من رغبتك في شراء هذه المادة؟
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    سيتم تفعيل المادة فوراً وإضافتها إلى قائمة "موادي" مع إمكانية قراءة جميع ملفات الشرح والمحاضرات.
                  </p>

                  <div className="mt-6 flex justify-end gap-2.5">
                    <button
                      type="button"
                      onClick={() => setShowPurchaseModal(false)}
                      disabled={purchasing}
                      className="rounded-xl border border-border px-4 py-2.5 text-sm font-semibold text-foreground hover:bg-muted"
                    >
                      إلغاء
                    </button>
                    <button
                      type="button"
                      onClick={handleConfirmPurchase}
                      disabled={purchasing}
                      className="rounded-xl bg-accent px-6 py-2.5 text-sm font-bold text-accent-foreground hover:brightness-105 disabled:opacity-50"
                    >
                      {purchasing ? "جاري التأكيد..." : "تأكيد الشراء"}
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    );
  }

  // If student HAS purchased the subject -> Show the 3 Tabs
  return (
    <div className="space-y-6">
      {/* 3 Tabs in Exact Order */}
      <div role="tablist" aria-label="أقسام المادة" className="grid grid-cols-3 border-b border-border text-center">
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

      {/* TAB 1: شرح المادة (Headings and PDFs) */}
      {activeTab === "content" && (
        <div className="space-y-4">
          {loading ? (
            <div className="p-12 text-center text-muted-foreground">جاري تحميل المحاضرات والملفات...</div>
          ) : sections.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-border p-10 text-center">
              <FileText className="mx-auto h-12 w-12 text-muted-foreground/40 mb-3" />
              <p className="font-bold text-foreground">لم يتم رفع ملفات لهذه المادة بعد</p>
              <p className="mt-1 text-xs text-muted-foreground">سيقوم مدرس المادة برفع المحاضرات قريباً.</p>
            </div>
          ) : (
            sections.map((section, idx) => (
              <div key={section.id} className="rounded-2xl border border-border bg-card shadow-soft overflow-hidden">
                <div className="flex items-center gap-2.5 border-b border-border/70 bg-muted/20 px-4 py-3 sm:px-5">
                  <span className="grid h-6 w-6 shrink-0 place-items-center rounded-md bg-accent text-accent-foreground text-xs font-bold">
                    {idx + 1}
                  </span>
                  <h4 className="font-bold text-foreground text-base">{section.title}</h4>
                </div>

                <div className="p-4 sm:p-5">
                  {(!section.files || section.files.length === 0) ? (
                    <p className="text-xs text-muted-foreground italic">لا توجد ملفات تحت هذا العنوان.</p>
                  ) : (
                    <div className="grid gap-2.5 sm:grid-cols-2">
                      {section.files.map((file) => (
                        <div
                          key={file.id}
                          className="flex items-center justify-between gap-3 rounded-xl border border-border bg-background p-3.5 hover:border-accent/50 transition"
                        >
                          <div className="flex items-center gap-3 min-w-0">
                            <span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-red-500/10 text-red-500">
                              <FileText className="h-5 w-5" />
                            </span>
                            <div className="min-w-0">
                              <p className="truncate text-sm font-bold text-foreground">{file.display_name}</p>
                              <span className="text-[10px] text-muted-foreground">مستند PDF مخصص للقراءة</span>
                            </div>
                          </div>

                          <button
                            type="button"
                            onClick={() => handleOpenPdf(file)}
                            className="inline-flex items-center gap-1.5 rounded-lg bg-ink px-3 py-1.5 text-xs font-bold text-ink-foreground shadow-sm hover:brightness-110"
                          >
                            <Eye className="h-3.5 w-3.5" />
                            <span>استعراض</span>
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))
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
            لا توجد أسئلة دورات مضافة لهذه المادة حالياً.
          </p>
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
