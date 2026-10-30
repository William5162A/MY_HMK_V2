import { Link } from "@tanstack/react-router";
import {
  Atom, BookOpen, Bot, Check, Cog, Cpu, Eye, FlaskConical, Gauge, Languages, Network, PenTool, Radio, ShieldCheck, Sigma, Thermometer, Zap,
  type LucideIcon,
} from "lucide-react";
import { SEM_NAMES, YEAR_NAMES, type Subject } from "@/lib/curriculum";
import { useStore } from "@/lib/store";

const RULES: [RegExp, LucideIcon][] = [
  [/رياضيات|نمذجة|إشارة/, Sigma],
  [/لغة|اللغة/, Languages],
  [/كيمياء/, FlaskConical],
  [/فيزياء|بصريات/, Atom],
  [/برمجة|حاسوب|حواسيب|متحكمات صغرية/, Cpu],
  [/شبكات|اتصال/, Network],
  [/الاتصالات/, Radio],
  [/كهربائية|القيادة|إلكترون/, Zap],
  [/حرارية|تدفئة|طاقة/, Thermometer],
  [/قياس|حساسات|تحكم/, Gauge],
  [/رسم|تصميم/, PenTool],
  [/روبوت|ذكاء|PLC|أتمتة/, Bot],
  [/صور/, Eye],
  [/آلات|ميكانيك|إنتاج|مواد|معادن/, Cog],
];

export const iconFor = (name: string) => RULES.find(([r]) => r.test(name))?.[1] ?? BookOpen;

interface SubjectCardProps {
  subject: Subject;
  isPublished?: boolean;
}

export function SubjectCard({ subject, isPublished = true }: SubjectCardProps) {
  const { isPurchased, role } = useStore();
  const owned = isPurchased(subject.id);
  const Icon = iconFor(subject.name);

  return (
    <Link
      to="/subjects/$subjectId"
      params={{ subjectId: subject.id }}
      className="group relative flex min-w-0 flex-col overflow-hidden rounded-2xl border border-border bg-card p-5 shadow-soft transition hover:-translate-y-0.5 hover:border-accent/50"
    >
      <div className="flex items-start justify-between gap-3">
        <span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-accent/10 text-accent transition group-hover:bg-accent group-hover:text-accent-foreground">
          <Icon className="h-5 w-5" />
        </span>
        <div className="text-left">
          <span className="num font-display text-3xl leading-none text-muted-foreground/30">
            {String(subject.year).padStart(2, "0")}
          </span>
        </div>
      </div>

      <h3 className="mt-4 text-lg font-bold leading-snug text-foreground">{subject.name}</h3>
      <p className="mt-1 text-xs font-semibold text-muted-foreground">
        {YEAR_NAMES[subject.year]} · {SEM_NAMES[subject.semester]}
      </p>

      {role === "admin" && (
        <div className="mt-2.5">
          <span
            className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-bold ${
              isPublished
                ? "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/25"
                : "bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/25"
            }`}
          >
            {isPublished ? "منشورة" : "مسودة (غير منشورة)"}
          </span>
        </div>
      )}

      <div className="mt-auto pt-5">
        <div className="flex items-center justify-between gap-2 border-t border-dashed border-border pt-4">
          {owned ? (
            <span className="inline-flex items-center gap-1.5 text-xs font-bold text-accent">
              <ShieldCheck className="h-4 w-4" /> مادة مشتراة
            </span>
          ) : (
            <span className="text-xs text-muted-foreground">مقرر دراسي معتمد</span>
          )}

          <span className="rounded-lg bg-ink px-3 py-1.5 text-xs font-bold text-ink-foreground group-hover:brightness-110">
            {role === "admin" ? "إدارة المحتوى" : owned ? "متابعة الدراسة" : "تصفح المادة"}
          </span>
        </div>
      </div>
    </Link>
  );
}
