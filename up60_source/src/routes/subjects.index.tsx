import { createFileRoute } from "@tanstack/react-router";
import { CheckCircle2, FileQuestion, Filter, Search, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { SEM_NAMES, SUBJECTS, YEAR_NAMES, type Semester, type Year } from "@/lib/curriculum";
import { subjectService } from "@/lib/services/subjectService";
import type { SubjectRecord } from "@/lib/services/types";
import { useStore } from "@/lib/store";
import { SubjectCard } from "@/components/SubjectCard";

export const Route = createFileRoute("/subjects/")({
  head: () => ({
    meta: [
      { title: "تصفح المواد — مِداد" },
      {
        name: "description",
        content: "تصفح جميع المواد الجامعية حسب السنة والفصل وابحث عنها بسهولة.",
      },
    ],
  }),
  component: Browse,
});

// Normalize Arabic for forgiving search.
const norm = (t: string) =>
  t
    .replace(/[\u064B-\u0652]/g, "")
    .replace(/[أإآ]/g, "ا")
    .replace(/ة/g, "ه")
    .replace(/ى/g, "ي")
    .toLowerCase()
    .trim();

function Browse() {
  const { role } = useStore();
  const [q, setQ] = useState("");
  const [sem, setSem] = useState<Semester | null>(null);
  const [year, setYear] = useState<Year | null>(null);
  const [publishedMap, setPublishedMap] = useState<Record<string, boolean>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    subjectService
      .getSubjects("admin")
      .then((records) => {
        const map: Record<string, boolean> = {};
        records.forEach((r) => {
          map[r.id] = r.is_published;
        });
        setPublishedMap(map);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const publishedCount = useMemo(() => {
    return Object.values(publishedMap).filter(Boolean).length;
  }, [publishedMap]);

  const unpublishedCount = useMemo(() => {
    return SUBJECTS.length - publishedCount;
  }, [publishedCount]);

  const list = useMemo(() => {
    const nq = norm(q);
    return SUBJECTS.filter((s) => {
      // For student: ONLY show published subjects
      if (role === "student" && !publishedMap[s.id]) {
        return false;
      }
      return (
        (year === null || s.year === year) &&
        (sem === null || s.semester === sem) &&
        (!nq || norm(s.name).includes(nq))
      );
    }).sort((a, b) => a.order - b.order);
  }, [q, sem, year, role, publishedMap]);

  return (
    <div className="space-y-8">
      {/* Admin Quick Catalog Stats Banner */}
      {role === "admin" && (
        <div className="grid gap-3 sm:grid-cols-3">
          <div className="rounded-2xl border border-border bg-card p-4 shadow-soft">
            <p className="text-xs text-muted-foreground font-semibold">إجمالي مواد الخطة الهندسية</p>
            <div className="num mt-1 font-display text-3xl font-bold text-foreground">{SUBJECTS.length} مادة</div>
          </div>
          <div className="rounded-2xl border border-emerald-500/25 bg-emerald-500/5 p-4 shadow-soft">
            <div className="flex items-center gap-1.5 text-xs font-bold text-emerald-600 dark:text-emerald-400">
              <CheckCircle2 className="h-4 w-4" /> المواد المنشورة للطلاب
            </div>
            <div className="num mt-1 font-display text-3xl font-bold text-emerald-600 dark:text-emerald-400">
              {publishedCount}
            </div>
          </div>
          <div className="rounded-2xl border border-amber-500/25 bg-amber-500/5 p-4 shadow-soft">
            <div className="flex items-center gap-1.5 text-xs font-bold text-amber-600 dark:text-amber-400">
              <FileQuestion className="h-4 w-4" /> المواد غير المنشورة (مسودة)
            </div>
            <div className="num mt-1 font-display text-3xl font-bold text-amber-600 dark:text-amber-400">
              {unpublishedCount}
            </div>
          </div>
        </div>
      )}

      {/* Header and Search Filters */}
      <header className="grid gap-6 border-b border-border pb-8 lg:grid-cols-[1fr_auto] lg:items-end">
        <div>
          <p className="text-xs font-bold text-accent">
            {role === "admin" ? "لوحة الإدارة — إدارة النشر والمحتوى" : "الخطة الدراسية الجامعية"}
          </p>
          <h1 className="mt-1 font-display text-4xl sm:text-5xl text-foreground">تصفح المواد</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            {role === "admin"
              ? "يمكنك استعراض جميع المواد ونشر أو حجب أي مادة وإدارة محتواها من الملفات."
              : "تصفح المقررات الدراسية المنشورة وابدأ دراستها مباشرة."}
          </p>
        </div>

        <div className="flex flex-col gap-3 sm:flex-row">
          <label className="relative block sm:w-80">
            <span className="sr-only">ابحث عن مادة</span>
            <Search className="pointer-events-none absolute right-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="ابحث عن مادة…"
              className="h-12 w-full rounded-xl border border-border bg-card pe-10 ps-10 text-sm outline-none focus:border-accent"
            />
            {q && (
              <button
                type="button"
                aria-label="مسح البحث"
                onClick={() => setQ("")}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            )}
          </label>
        </div>
      </header>

      {/* Filters (Years & Semesters) */}
      <div className="flex flex-wrap items-center gap-3">
        {/* Year Selector */}
        <div className="flex flex-wrap items-center gap-1.5 rounded-2xl border border-border bg-card p-1">
          <button
            type="button"
            onClick={() => setYear(null)}
            className={`rounded-xl px-3.5 py-2 text-xs font-bold transition ${
              year === null ? "bg-ink text-ink-foreground shadow-soft" : "text-muted-foreground hover:text-foreground"
            }`}
          >
            كل السنوات
          </button>
          {([1, 2, 3, 4, 5] as Year[]).map((v) => (
            <button
              key={v}
              type="button"
              onClick={() => setYear(year === v ? null : v)}
              className={`rounded-xl px-3 py-2 text-xs font-bold transition ${
                year === v ? "bg-ink text-ink-foreground shadow-soft" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {YEAR_NAMES[v]}
            </button>
          ))}
        </div>

        {/* Semester Selector */}
        <div className="flex items-center gap-1.5 rounded-2xl border border-border bg-card p-1">
          <button
            type="button"
            onClick={() => setSem(null)}
            className={`rounded-xl px-3.5 py-2 text-xs font-bold transition ${
              sem === null ? "bg-ink text-ink-foreground shadow-soft" : "text-muted-foreground hover:text-foreground"
            }`}
          >
            كل الفصول
          </button>
          {([1, 2] as Semester[]).map((v) => (
            <button
              key={v}
              type="button"
              onClick={() => setSem(sem === v ? null : v)}
              className={`rounded-xl px-3 py-2 text-xs font-bold transition ${
                sem === v ? "bg-ink text-ink-foreground shadow-soft" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {SEM_NAMES[v]}
            </button>
          ))}
        </div>
      </div>

      {/* Grid of Subject Cards */}
      {loading ? (
        <div className="p-16 text-center text-muted-foreground">جاري تحميل المواد...</div>
      ) : list.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-border p-12 text-center">
          <Filter className="mx-auto h-12 w-12 text-muted-foreground/40 mb-3" />
          <h3 className="font-display text-xl font-bold text-foreground">لا توجد مواد مطابقة للبحث</h3>
          <p className="mt-1 text-sm text-muted-foreground">
            {role === "student"
              ? "لم يتم نشر مواد مطابقة في هذا التصنيف بعد."
              : "جرب تغيير مصطلحات البحث أو الفلاتر المحددة."}
          </p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {list.map((subject) => (
            <SubjectCard
              key={subject.id}
              subject={subject}
              isPublished={publishedMap[subject.id] ?? false}
            />
          ))}
        </div>
      )}
    </div>
  );
}
