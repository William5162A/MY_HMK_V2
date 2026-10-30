import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { BookOpen, GraduationCap, LogOut, Moon, Shield, Sun } from "lucide-react";
import { SEM_NAMES, YEAR_NAMES, type Semester, type Year } from "@/lib/curriculum";
import { useStore } from "@/lib/store";

export const Route = createFileRoute("/profile")({
  head: () => ({
    meta: [
      { title: "ملفي الشخصي — مِداد" },
      { name: "description", content: "معلومات الحساب والصلاحيات وإعدادات المظهر." },
    ],
  }),
  component: Profile,
});

function Profile() {
  const s = useStore();
  const navigate = useNavigate();

  async function handleLogout() {
    await s.logout();
    navigate({ to: "/login" });
  }

  const displayName = s.profile?.display_name || (s.role === "admin" ? "المشرف العام" : "طالب جامعي");
  const email = s.user?.email || (s.role === "admin" ? "admin@midad.edu" : "user@midad.edu");

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-4 py-8">
      {/* Profile Header & Role Badge Card */}
      <section className="rounded-3xl border border-border bg-card p-6 shadow-soft sm:p-8 text-center relative overflow-hidden">
        <div
          className={`mx-auto grid h-20 w-20 place-items-center rounded-2xl font-display text-3xl font-bold shadow-soft ${
            s.role === "admin"
              ? "bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/30"
              : "bg-accent/15 text-accent border border-accent/30"
          }`}
        >
          {s.role === "admin" ? <Shield className="h-10 w-10" /> : <GraduationCap className="h-10 w-10" />}
        </div>

        <h1 className="mt-4 font-display text-3xl font-bold text-foreground">{displayName}</h1>
        <p className="mt-1 text-sm text-muted-foreground">{email}</p>

        {/* Prominent Role Indicator */}
        <div className="mt-4 inline-flex items-center gap-2 rounded-full px-4 py-1.5 text-xs font-bold shadow-sm">
          {s.role === "admin" ? (
            <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-500/15 px-3 py-1 text-amber-600 dark:text-amber-400 border border-amber-500/30">
              <Shield className="h-4 w-4" /> مسؤول النظام (Administrator)
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 rounded-full bg-accent/15 px-3 py-1 text-accent border border-accent/30">
              <GraduationCap className="h-4 w-4" /> طالب جامعي (Student)
            </span>
          )}
        </div>

        {/* Stats Row */}
        <div className="mt-8 grid grid-cols-2 gap-3 border-t border-border pt-6 text-center sm:grid-cols-2">
          <div>
            <div className="num font-display text-3xl font-bold text-foreground">{s.purchased.length}</div>
            <div className="text-xs text-muted-foreground mt-0.5">مواد مفعّلة في الحساب</div>
          </div>
          <div>
            <div className="num font-display text-3xl font-bold text-foreground">
              {s.role === "admin" ? "كامل الصلاحيات" : `${YEAR_NAMES[s.year]}`}
            </div>
            <div className="text-xs text-muted-foreground mt-0.5">
              {s.role === "admin" ? "لوحة الإدارة والنشر" : SEM_NAMES[s.semester]}
            </div>
          </div>
        </div>
      </section>

      {/* Academic Settings & Theme Section */}
      <section className="space-y-6 rounded-3xl border border-border bg-card p-6 shadow-soft sm:p-8">
        {s.role === "student" && (
          <>
            <div>
              <h2 className="text-lg font-bold text-foreground">السنة الدراسية</h2>
              <p className="text-sm text-muted-foreground">
                تُبنى المواد المقترحة في الصفحة الرئيسية على سنتك وفصلك الحاليين.
              </p>
              <div role="radiogroup" aria-label="السنة الدراسية" className="mt-4 grid grid-cols-5 gap-2">
                {([1, 2, 3, 4, 5] as Year[]).map((y) => (
                  <button
                    key={y}
                    type="button"
                    role="radio"
                    aria-checked={s.year === y}
                    onClick={() => s.setYear(y)}
                    className="h-12 rounded-xl border border-border text-sm font-bold aria-checked:border-ink aria-checked:bg-ink aria-checked:text-ink-foreground transition-colors hover:border-accent"
                  >
                    <span>{y}</span>
                  </button>
                ))}
              </div>
              <p className="mt-2 text-sm font-semibold text-accent">{YEAR_NAMES[s.year]}</p>
            </div>

            <div>
              <h2 className="text-lg font-bold text-foreground">الفصل الدراسي</h2>
              <div role="radiogroup" aria-label="الفصل الدراسي" className="mt-4 grid grid-cols-2 gap-2">
                {([1, 2] as Semester[]).map((v) => (
                  <button
                    key={v}
                    type="button"
                    role="radio"
                    aria-checked={s.semester === v}
                    onClick={() => s.setSemester(v)}
                    className="h-12 rounded-xl border border-border text-sm font-bold aria-checked:border-ink aria-checked:bg-ink aria-checked:text-ink-foreground transition-colors hover:border-accent"
                  >
                    {SEM_NAMES[v]}
                  </button>
                ))}
              </div>
            </div>
          </>
        )}

        {/* Theme Preference (Light / Dark) */}
        <div>
          <h2 className="text-lg font-bold text-foreground">المظهر والسمة</h2>
          <p className="text-sm text-muted-foreground">اختر وضع الألوان المفضل لك للقراءة والدراسة.</p>
          <div role="radiogroup" aria-label="المظهر" className="mt-4 grid grid-cols-2 gap-3">
            <button
              type="button"
              role="radio"
              aria-checked={s.theme === "light"}
              onClick={() => s.setTheme("light")}
              className="flex items-center justify-center gap-2 h-12 rounded-xl border border-border text-sm font-bold aria-checked:border-ink aria-checked:bg-ink aria-checked:text-ink-foreground transition-colors hover:border-accent"
            >
              <Sun className="h-4 w-4" />
              <span>الوضع الفاتح</span>
            </button>
            <button
              type="button"
              role="radio"
              aria-checked={s.theme === "dark"}
              onClick={() => s.setTheme("dark")}
              className="flex items-center justify-center gap-2 h-12 rounded-xl border border-border text-sm font-bold aria-checked:border-ink aria-checked:bg-ink aria-checked:text-ink-foreground transition-colors hover:border-accent"
            >
              <Moon className="h-4 w-4" />
              <span>الوضع الداكن</span>
            </button>
          </div>
        </div>

        {/* Logout Button */}
        <div className="border-t border-border pt-6">
          <button
            type="button"
            onClick={handleLogout}
            className="flex min-h-12 w-full items-center justify-center gap-2 rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 font-bold text-destructive transition-colors hover:bg-destructive/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-destructive"
          >
            <LogOut className="h-5 w-5" />
            <span>تسجيل الخروج من المنصة</span>
          </button>
          <p className="mt-2 text-center text-xs text-muted-foreground">
            يمكنك تسجيل الدخول مجدداً بحساب المشرف أو حساب الطالب.
          </p>
        </div>
      </section>
    </div>
  );
}
