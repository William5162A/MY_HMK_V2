import { createFileRoute, Link, Navigate } from "@tanstack/react-router";
import { ArrowLeft, BookOpen, CheckCircle2, GraduationCap, Layers, ShieldCheck, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";
import { recommend, SEM_NAMES, subjectById, YEAR_NAMES } from "@/lib/curriculum";
import { subjectService } from "@/lib/services/subjectService";
import type { SubjectRecord } from "@/lib/services/types";
import { useStore } from "@/lib/store";
import { SubjectCard } from "@/components/SubjectCard";
import { SectionTitle } from "@/components/Bits";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "الرئيسية — مِداد" },
      { name: "description", content: "تابع دراستك في موادك الجامعية الهندسية مع مِداد." },
      { property: "og:title", content: "مِداد — رحلتك نحو التفوق" },
    ],
  }),
  component: Home,
});

function Home() {
  const s = useStore();
  const [publishedMap, setPublishedMap] = useState<Record<string, boolean>>({});

  useEffect(() => {
    subjectService.getSubjects("admin").then((records) => {
      const map: Record<string, boolean> = {};
      records.forEach((r) => {
        map[r.id] = r.is_published;
      });
      setPublishedMap(map);
    });
  }, []);

  if (!s.ready) {
    return null;
  }

  if (!s.isLoggedIn) {
    return <Navigate to="/login" />;
  }

  const owned = new Set(s.purchased);
  const enrolledSubjects = s.purchased.map((id) => subjectById(id)).filter(Boolean);
  const recs = recommend(s.year, s.semester, owned).filter((sub) => s.role === "admin" || publishedMap[sub.id]);
  const displayName = s.profile?.display_name || (s.role === "admin" ? "المشرف العام" : "طالب");

  const stats = [
    {
      label: "المواد المفعّلة",
      value: s.purchased.length,
      icon: Layers,
      to: "/purchases" as const,
    },
    {
      label: "السنة الدراسية",
      value: YEAR_NAMES[s.year],
      icon: GraduationCap,
      to: "/profile" as const,
    },
    {
      label: "الفصل الدراسي",
      value: SEM_NAMES[s.semester],
      icon: BookOpen,
      to: "/profile" as const,
    },
    {
      label: "صلاحية الحساب",
      value: s.role === "admin" ? "مسؤول النظام" : "طالب جامعي",
      icon: s.role === "admin" ? ShieldCheck : CheckCircle2,
      to: "/profile" as const,
    },
  ];

  return (
    <div className="space-y-10">
      {/* Hero Welcome Card */}
      <section className="relative grid gap-4 lg:grid-cols-[1.4fr_1fr]">
        <div className="motif relative overflow-hidden rounded-3xl bg-ink-gradient p-7 text-ink-foreground sm:p-10 shadow-lift">
          <p className="text-sm font-semibold text-ink-muted">
            {YEAR_NAMES[s.year]} · {SEM_NAMES[s.semester]}
          </p>

          <h1 className="mt-3 font-display text-3xl font-bold leading-tight sm:text-4xl lg:text-5xl">
            أهلًا بعودتك، {displayName}
          </h1>

          <p className="mt-3 max-w-md text-sm sm:text-base leading-relaxed text-ink-muted">
            {s.role === "admin"
              ? "أنت في وضع المشرف العام: يمكنك نشر المقررات وإضافة العناوين ورفع ملفات الـ PDF."
              : "رحلتك نحو التفوق الأكاديمي تبدأ بخطوة صغيرة اليوم."}
          </p>

          <div className="mt-8 flex flex-wrap gap-3">
            <Link
              to="/subjects"
              className="inline-flex items-center gap-2 rounded-xl bg-accent px-5 py-3 text-sm font-bold text-accent-foreground transition hover:brightness-105"
            >
              <span>{s.role === "admin" ? "إدارة وتصفح المواد" : "تصفح المواد المنشورة"}</span>
              <ArrowLeft className="h-4 w-4" />
            </Link>

            <Link
              to="/purchases"
              className="inline-flex items-center rounded-xl border border-ink-muted/30 px-5 py-3 text-sm font-bold text-ink-foreground hover:bg-white/5 transition"
            >
              موادي الدراسية
            </Link>
          </div>
        </div>

        {/* 4 Stats Cards Grid */}
        <div className="grid grid-cols-2 gap-3">
          {stats.map((st, i) => (
            <Link
              key={st.label}
              to={st.to}
              className={`flex flex-col justify-between rounded-2xl border border-border p-4 shadow-soft transition hover:-translate-y-0.5 sm:p-5 ${
                i === 0 ? "bg-accent text-accent-foreground" : "bg-card"
              }`}
            >
              <st.icon className="h-5 w-5 opacity-70" />

              <div className="mt-4">
                <div className="font-display text-xl sm:text-2xl font-bold truncate">{st.value}</div>
                <div className={`mt-1 text-xs font-semibold ${i === 0 ? "" : "text-muted-foreground"}`}>
                  {st.label}
                </div>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Enrolled Subjects Section ("موادي قيد الدراسة") */}
      {enrolledSubjects.length > 0 && (
        <section className="space-y-4">
          <SectionTitle kicker="مكتبتك الخاصة" title="موادي قيد الدراسة" />
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {enrolledSubjects.map((sub) => (
              <SubjectCard key={sub!.id} subject={sub!} />
            ))}
          </div>
        </section>
      )}

      {/* Recommended Subjects Section */}
      <section className="space-y-4">
        <SectionTitle
          kicker={`حسب سنتك وفصلك (${YEAR_NAMES[s.year]} · ${SEM_NAMES[s.semester]})`}
          title="مواد مقترحة لدراستها"
          action={
            <Link to="/profile" className="text-sm font-bold text-accent hover:underline">
              تغيير الفصل
            </Link>
          }
        />

        {recs.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {recs.slice(0, 6).map((r) => (
              <SubjectCard key={r.id} subject={r} isPublished={publishedMap[r.id] ?? false} />
            ))}
          </div>
        ) : (
          <div className="rounded-2xl border border-dashed border-border p-8 text-center text-muted-foreground">
            {s.role === "student"
              ? "لم يتم نشر مواد إضافية في هذا الفصل بعد."
              : "أحسنت! جميع مواد هذا الفصل منشورة ومتاحة."}
          </div>
        )}
      </section>
    </div>
  );
}
