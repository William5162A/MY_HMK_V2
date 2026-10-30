import { createFileRoute, Link, notFound } from "@tanstack/react-router";
import { ArrowRight, BookOpen, ShieldCheck } from "lucide-react";
import { useState, useEffect } from "react";
import { SEM_NAMES, subjectById, YEAR_NAMES } from "@/lib/curriculum";
import { subjectService } from "@/lib/services/subjectService";
import { useStore } from "@/lib/store";
import { AdminSubjectContent } from "@/components/AdminSubjectContent";
import { StudentSubjectContent } from "@/components/StudentSubjectContent";

export const Route = createFileRoute("/subjects/$subjectId")({
  loader: ({ params }) => {
    const subject = subjectById(params.subjectId);
    if (!subject) throw notFound();
    return { subject };
  },
  head: ({ loaderData }) => ({
    meta: loaderData
      ? [
          { title: `${loaderData.subject.name} — مِداد` },
          { name: "description", content: `مادة ${loaderData.subject.name} في منصة مِداد التعليمية` },
        ]
      : [{ title: "المادة غير متاحة — مِداد" }],
  }),
  component: SubjectPage,
});

function SubjectPage() {
  const { subject } = Route.useLoaderData();
  const { role, isPurchased } = useStore();
  const owned = isPurchased(subject.id);
  const [isPublished, setIsPublished] = useState(false);

  useEffect(() => {
    // Check subject status
    subjectService.getSubjects("admin").then((subs) => {
      const found = subs.find((s) => s.id === subject.id);
      if (found) {
        setIsPublished(found.is_published);
      }
    });
  }, [subject.id]);

  const handleTogglePublish = async () => {
    const updated = await subjectService.togglePublish(subject.id, isPublished);
    setIsPublished(updated);
  };

  return (
    <div className="space-y-6">
      {/* Breadcrumb Navigation */}
      <Link
        to="/subjects"
        className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted-foreground hover:text-foreground transition"
      >
        <ArrowRight className="h-4 w-4" /> العودة إلى تصفح المواد
      </Link>

      {/* Main Subject Header Banner */}
      <header className="motif grid gap-6 overflow-hidden rounded-3xl bg-ink-gradient p-6 text-ink-foreground sm:p-9 lg:grid-cols-[1fr_auto] lg:items-end">
        <div className="min-w-0">
          <div className="flex items-center gap-3">
            <span className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground font-bold">
              <BookOpen className="h-6 w-6" />
            </span>
            <p className="text-sm font-semibold text-ink-muted">
              {YEAR_NAMES[subject.year]} · {SEM_NAMES[subject.semester]}
            </p>
          </div>

          <h1 className="mt-5 font-display text-3xl leading-tight sm:text-5xl">{subject.name}</h1>
          <p className="mt-2 text-sm text-ink-muted">
            المنهج الجامعي المعتمد · الهندسة والميكاترونيكس
          </p>
        </div>

        {role === "student" && (
          <div className="flex items-center gap-3">
            {owned ? (
              <div className="rounded-2xl border border-accent/40 bg-accent/15 px-4 py-2.5 text-accent font-bold text-sm inline-flex items-center gap-2">
                <ShieldCheck className="h-5 w-5" />
                <span>مادة مشتراة في حسابك</span>
              </div>
            ) : (
              <span className="text-xs text-ink-muted">محتوى غير مفعّل بعد</span>
            )}
          </div>
        )}
      </header>

      {/* Role-Based Subject Content Display */}
      {role === "admin" ? (
        <AdminSubjectContent
          subjectId={subject.id}
          subjectName={subject.name}
          isPublished={isPublished}
          onTogglePublish={handleTogglePublish}
        />
      ) : (
        <StudentSubjectContent
          subjectId={subject.id}
          subjectName={subject.name}
          isOwned={owned}
        />
      )}
    </div>
  );
}
