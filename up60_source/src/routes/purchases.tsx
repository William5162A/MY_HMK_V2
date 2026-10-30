import { createFileRoute, Link } from "@tanstack/react-router";
import { BookOpen, ShoppingBag } from "lucide-react";
import { SUBJECTS } from "@/lib/curriculum";
import { useStore } from "@/lib/store";
import { SubjectCard } from "@/components/SubjectCard";

export const Route = createFileRoute("/purchases")({
  head: () => ({
    meta: [
      { title: "موادي — مِداد" },
      { name: "description", content: "المواد التي سجلت بها ومحاضراتك المتاحة." },
      { property: "og:title", content: "موادي — مِداد" },
    ],
  }),
  component: Purchases,
});

function Purchases() {
  const { purchased, role } = useStore();
  const list = SUBJECTS.filter((s) => purchased.includes(s.id));

  return (
    <div className="space-y-6">
      <header className="border-b border-border pb-6">
        <p className="text-xs font-bold text-accent">مكتبتك الأكاديمية</p>
        <h1 className="mt-1 font-display text-4xl sm:text-5xl text-foreground">موادي الدراسية</h1>
        <p className="num mt-2 text-sm text-muted-foreground">{list.length} مادة مفعّلة في حسابك</p>
      </header>

      {list.length ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {list.map((s) => (
            <SubjectCard key={s.id} subject={s} />
          ))}
        </div>
      ) : (
        <div className="rounded-3xl border border-dashed border-border p-12 text-center">
          <div className="mx-auto grid h-14 w-14 place-items-center rounded-2xl bg-accent/15 text-accent mb-3">
            <ShoppingBag className="h-7 w-7" />
          </div>
          <p className="text-lg font-bold text-foreground">لم تقم بإضافة أي مادة إلى موادك بعد</p>
          <p className="mt-1 text-sm text-muted-foreground max-w-sm mx-auto">
            تصفح المواد المنشورة وأضف مقررات فصلك الحالي للبدء بالدراسة ومتابعة المحاضرات.
          </p>
          <Link
            to="/subjects"
            className="mt-6 inline-flex items-center gap-2 rounded-xl bg-accent px-6 py-2.5 text-sm font-bold text-accent-foreground shadow-soft hover:brightness-105 transition"
          >
            <BookOpen className="h-4 w-4" />
            <span>تصفح المواد لإضافتها</span>
          </Link>
        </div>
      )}
    </div>
  );
}
