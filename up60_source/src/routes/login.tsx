import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { ArrowLeft, BookOpen, Eye, EyeOff, GraduationCap, LockKeyhole, Mail, ShieldCheck, Sparkles, UserPlus } from "lucide-react";
import { useEffect, useState, type FormEvent } from "react";
import { ThemeToggle } from "@/components/AppShell";
import { authService } from "@/lib/services/authService";
import { useStore } from "@/lib/store";

export const Route = createFileRoute("/login")({
  head: () => ({
    meta: [
      { title: "تسجيل الدخول — مِداد" },
      { name: "description", content: "سجّل دخولك إلى منصة مِداد التعليمية." },
    ],
  }),
  component: LoginPage,
});

function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoggedIn, ready } = useStore();

  const [mode, setMode] = useState<"login" | "register">("login");
  const [identifier, setIdentifier] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (ready && isLoggedIn) {
      navigate({ to: "/" });
    }
  }, [ready, isLoggedIn, navigate]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    const cleanId = identifier.trim();
    if (!cleanId || !password.trim()) {
      setError("يرجى إدخال اسم المستخدم أو البريد الإلكتروني وكلمة المرور.");
      return;
    }

    try {
      setSubmitting(true);
      if (mode === "login") {
        await login(cleanId, password.trim());
      } else {
        // Public registration: assigns 'student' role only
        if (!displayName.trim()) {
          setError("يرجى إدخال الاسم الكامل للطالب.");
          setSubmitting(false);
          return;
        }
        const email = cleanId.includes("@") ? cleanId : `${cleanId}@midad.edu`;
        await authService.signUp(email, password.trim(), displayName.trim());
        // Automatically login the new student
        await login(email, password.trim());
      }

      navigate({ to: "/" });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "فشل تسجيل الدخول. يرجى التأكد من صحة البيانات.";
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  // Quick Demo account fillers
  const handleFillDemo = (type: "admin" | "user") => {
    setMode("login");
    setError("");
    if (type === "admin") {
      setIdentifier("admin");
      setPassword("123456");
    } else {
      setIdentifier("user");
      setPassword("123456");
    }
  };

  return (
    <main
      dir="rtl"
      className="relative flex min-h-screen items-center justify-center overflow-hidden bg-background px-4 py-8 sm:px-6"
    >
      {/* Decorative blurred backgrounds */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -right-28 -top-28 h-80 w-80 rounded-full bg-accent/10 blur-3xl"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-32 -left-24 h-96 w-96 rounded-full bg-primary/10 blur-3xl"
      />

      {/* Theme Toggle Button */}
      <div className="absolute left-4 top-4 z-10 sm:left-6 sm:top-6">
        <ThemeToggle />
      </div>

      <div className="relative z-10 grid w-full max-w-5xl overflow-hidden rounded-3xl border border-border bg-card shadow-lift lg:grid-cols-2">
        {/* Left Side: Brand & Hero Showcase */}
        <section className="motif relative hidden overflow-hidden bg-ink-gradient p-10 text-ink-foreground lg:flex lg:flex-col lg:justify-between xl:p-12">
          <div>
            <div className="inline-flex items-center gap-3">
              <span className="grid h-12 w-12 place-items-center rounded-2xl bg-accent text-accent-foreground font-bold">
                <BookOpen className="h-6 w-6" />
              </span>
              <span className="font-display text-3xl font-bold">مِداد</span>
            </div>

            <div className="mt-16">
              <span className="inline-flex items-center gap-2 rounded-full border border-ink-muted/25 px-3 py-1.5 text-xs font-semibold text-ink-muted">
                <GraduationCap className="h-4 w-4" />
                منصتك الجامعية المعتمدة
              </span>

              <h1 className="mt-6 font-display text-4xl leading-[1.6] xl:text-5xl font-bold">
                كل ما تحتاجه
                <br />
                لرحلتك الجامعية
                <br />
                في مكان واحد.
              </h1>

              <p className="mt-5 max-w-sm text-sm leading-8 text-ink-muted">
                نظّم موادك الهندسية، وتابع دروسك وملخصاتك من خلال ملفات PDF مخصصة للقراءة مع حماية مشددة على المحتوى.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3 border-t border-ink-muted/20 pt-5 text-sm text-ink-muted">
            <ShieldCheck className="h-5 w-5 shrink-0 text-accent" />
            <span>نظام آمن متكامل مدعوم بقواعد حماية Supabase RLS.</span>
          </div>
        </section>

        {/* Right Side: Auth Form */}
        <section className="flex min-w-0 flex-col justify-center p-6 sm:p-10 lg:p-12">
          <div className="mb-6">
            <div className="mb-4 grid h-12 w-12 place-items-center rounded-2xl bg-accent/15 text-accent">
              <GraduationCap className="h-6 w-6" />
            </div>

            <p className="text-xs font-bold text-accent">مرحبًا بك في منصة مِداد</p>
            <h2 className="mt-1 font-display text-2xl font-bold text-foreground sm:text-3xl">
              {mode === "login" ? "تسجيل الدخول" : "إنشاء حساب طالب جديد"}
            </h2>
            <p className="mt-2 text-xs leading-6 text-muted-foreground">
              {mode === "login"
                ? "أدخل اسم المستخدم أو البريد الإلكتروني وكلمة المرور للوصول إلى المنصة."
                : "سجل حسابك كطالب جامعي للوصول إلى فهرس المقررات المنشورة."}
            </p>
          </div>

          {/* Quick Demo Fill Buttons Chips */}
          <div className="mb-5 rounded-2xl border border-border bg-muted/30 p-3">
            <div className="flex items-center gap-1.5 text-[11px] font-bold text-muted-foreground mb-2">
              <Sparkles className="h-3.5 w-3.5 text-accent" />
              <span>تسجيل سريع بالحسابات المعتمدة (تعبئة فورية):</span>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => handleFillDemo("admin")}
                className="flex items-center justify-center gap-1.5 rounded-xl border border-amber-500/30 bg-amber-500/10 px-2.5 py-1.5 text-xs font-bold text-amber-600 dark:text-amber-400 hover:bg-amber-500/20 transition"
              >
                <span>حساب أدمن: admin</span>
              </button>
              <button
                type="button"
                onClick={() => handleFillDemo("user")}
                className="flex items-center justify-center gap-1.5 rounded-xl border border-accent/30 bg-accent/10 px-2.5 py-1.5 text-xs font-bold text-accent hover:bg-accent/20 transition"
              >
                <span>حساب طالب: user</span>
              </button>
            </div>
            <p className="text-[10px] text-muted-foreground mt-1.5 text-center">كلمة المرور لكلا الحسابين هي: 123456</p>
          </div>

          {/* Login / Register Toggle Tabs */}
          <div className="mb-5 grid grid-cols-2 gap-1 rounded-xl border border-border bg-muted/40 p-1">
            <button
              type="button"
              onClick={() => { setMode("login"); setError(""); }}
              className={`rounded-lg py-2 text-xs font-bold transition ${
                mode === "login" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              تسجيل الدخول
            </button>
            <button
              type="button"
              onClick={() => { setMode("register"); setError(""); }}
              className={`rounded-lg py-2 text-xs font-bold transition ${
                mode === "register" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              إنشاء حساب طالب
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {mode === "register" && (
              <div>
                <label className="mb-1.5 block text-xs font-semibold text-foreground">الاسم الكامل للطالب</label>
                <div className="relative">
                  <UserPlus className="pointer-events-none absolute right-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <input
                    type="text"
                    placeholder="مثال: أحمد العلي"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    className="h-11 w-full rounded-xl border border-border bg-background pr-10 pl-4 text-sm text-foreground outline-none focus:border-accent"
                    required={mode === "register"}
                  />
                </div>
              </div>
            )}

            <div>
              <label className="mb-1.5 block text-xs font-semibold text-foreground">
                اسم المستخدم أو البريد الإلكتروني
              </label>
              <div className="relative">
                <Mail className="pointer-events-none absolute right-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <input
                  type="text"
                  dir="ltr"
                  placeholder="admin أو user أو email@example.com"
                  value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  className="h-11 w-full rounded-xl border border-border bg-background pr-10 pl-4 text-left text-sm text-foreground outline-none focus:border-accent"
                  required
                />
              </div>
            </div>

            <div>
              <label className="mb-1.5 block text-xs font-semibold text-foreground">كلمة المرور</label>
              <div className="relative">
                <LockKeyhole className="pointer-events-none absolute right-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="كلمة المرور (123456)"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="h-11 w-full rounded-xl border border-border bg-background px-10 text-right text-sm text-foreground outline-none focus:border-accent"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-2.5 text-xs text-destructive">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="flex h-11 w-full items-center justify-center gap-2 rounded-xl bg-ink px-5 text-sm font-bold text-ink-foreground shadow-soft hover:brightness-110 transition disabled:opacity-50"
            >
              <span>{submitting ? "جاري المعالجة..." : mode === "login" ? "دخول إلى المنصة" : "تأكيد تسجيل الحساب"}</span>
              <ArrowLeft className="h-4 w-4" />
            </button>
          </form>
        </section>
      </div>
    </main>
  );
}
