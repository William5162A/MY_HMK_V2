import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { Semester, Year } from "./curriculum";
import { authService } from "./services/authService";
import { purchaseService } from "./services/purchaseService";
import type { AppRole, Profile } from "./services/types";

const THEME_KEY = "midad-theme-v1";

interface Ctx {
  ready: boolean;
  isLoggedIn: boolean;
  user: { id: string; email: string } | null;
  role: AppRole;
  profile: Profile | null;
  purchased: string[];
  year: Year;
  semester: Semester;
  theme: "light" | "dark";
  isPurchased: (subjectId: string) => boolean;
  purchase: (subjectId: string) => Promise<void>;
  setYear: (y: Year) => void;
  setSemester: (s: Semester) => void;
  setTheme: (t: "light" | "dark") => void;
  login: (identifier: string, pass: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshPurchases: () => Promise<void>;
  totals: {
    purchasedCount: number;
    year: number;
    semester: number;
  };
}

const StoreCtx = createContext<Ctx | null>(null);

export function StoreProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  const [user, setUser] = useState<{ id: string; email: string } | null>(null);
  const [role, setRole] = useState<AppRole>("student");
  const [profile, setProfile] = useState<Profile | null>(null);
  const [purchased, setPurchased] = useState<string[]>([]);
  const [year, setYearState] = useState<Year>(1);
  const [semester, setSemesterState] = useState<Semester>(1);
  
  // Retain localStorage ONLY for non-sensitive UI preferences (theme)
  const [theme, setThemeState] = useState<"light" | "dark">(() => {
    try {
      const saved = localStorage.getItem(THEME_KEY);
      if (saved === "dark" || saved === "light") return saved;
    } catch {
      /* ignore */
    }
    return "light";
  });

  // Apply theme to document
  useEffect(() => {
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch {
      /* ignore */
    }
    document.documentElement.classList.toggle("dark", theme === "dark");
  }, [theme]);

  // Load initial session on mount
  useEffect(() => {
    // Start with clean ready state
    setReady(true);
  }, []);

  const refreshPurchases = useCallback(async () => {
    if (!user) {
      setPurchased([]);
      return;
    }
    try {
      const items = await purchaseService.getUserPurchases(user.id);
      setPurchased(items);
    } catch (err) {
      console.error("Failed to load purchases", err);
    }
  }, [user]);

  const login = useCallback(async (identifier: string, pass: string) => {
    const res = await authService.signIn(identifier, pass);
    setUser(res.user);
    setRole(res.role);
    setProfile(res.profile);
    setYearState((res.profile.academic_year as Year) || 1);
    setSemesterState((res.profile.semester as Semester) || 1);

    // Fetch user purchases
    try {
      const items = await purchaseService.getUserPurchases(res.user.id);
      setPurchased(items);
    } catch {
      setPurchased([]);
    }
  }, []);

  const logout = useCallback(async () => {
    await authService.signOut();
    setUser(null);
    setRole("student");
    setProfile(null);
    setPurchased([]);
  }, []);

  const purchase = useCallback(async (subjectId: string) => {
    if (!user) throw new Error("يرجى تسجيل الدخول أولاً لإتمام الشراء.");
    await purchaseService.simulatePurchase(user.id, subjectId);
    setPurchased((prev) => Array.from(new Set([...prev, subjectId])));
  }, [user]);

  const isPurchased = useCallback((subjectId: string) => {
    return purchased.includes(subjectId);
  }, [purchased]);

  const setYear = useCallback((y: Year) => {
    setYearState(y);
    if (user) {
      authService.updateProfile(user.id, { academic_year: y });
    }
  }, [user]);

  const setSemester = useCallback((s: Semester) => {
    setSemesterState(s);
    if (user) {
      authService.updateProfile(user.id, { semester: s });
    }
  }, [user]);

  const setTheme = useCallback((t: "light" | "dark") => {
    setThemeState(t);
  }, []);

  const value = useMemo<Ctx>(() => ({
    ready,
    isLoggedIn: Boolean(user),
    user,
    role,
    profile,
    purchased,
    year,
    semester,
    theme,
    isPurchased,
    purchase,
    setYear,
    setSemester,
    setTheme,
    login,
    logout,
    refreshPurchases,
    totals: {
      purchasedCount: purchased.length,
      year,
      semester,
    },
  }), [ready, user, role, profile, purchased, year, semester, theme, isPurchased, purchase, setYear, setSemester, setTheme, login, logout, refreshPurchases]);

  return <StoreCtx.Provider value={value}>{children}</StoreCtx.Provider>;
}

export function useStore() {
  const ctx = useContext(StoreCtx);
  if (!ctx) throw new Error("useStore must be used within StoreProvider");
  return ctx;
}
