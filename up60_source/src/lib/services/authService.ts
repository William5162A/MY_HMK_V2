import { isSupabaseConfigured, supabase } from "../supabase";
import type { AppRole, Profile } from "./types";

// In-memory / session cache for offline or mock fallback
const DEMO_USERS: Record<string, { role: AppRole; name: string }> = {
  "admin": { role: "admin", name: "المشرف العام" },
  "admin@midad.edu": { role: "admin", name: "المشرف العام" },
  "user": { role: "student", name: "طالب جامعي" },
  "user@midad.edu": { role: "student", name: "طالب جامعي" },
};

export const authService = {
  async signIn(identifier: string, pass: string): Promise<{ user: { id: string; email: string }; role: AppRole; profile: Profile }> {
    const cleanId = identifier.trim().toLowerCase();
    
    // Check if it matches demo credentials
    if ((cleanId === "admin" || cleanId === "admin@midad.edu") && pass === "123456") {
      const demoId = "00000000-0000-0000-0000-000000000001";
      return {
        user: { id: demoId, email: "admin@midad.edu" },
        role: "admin",
        profile: { id: demoId, display_name: "المشرف العام (Admin)", academic_year: 3, semester: 1 },
      };
    }
    
    if ((cleanId === "user" || cleanId === "user@midad.edu") && pass === "123456") {
      const demoId = "00000000-0000-0000-0000-000000000002";
      return {
        user: { id: demoId, email: "user@midad.edu" },
        role: "student",
        profile: { id: demoId, display_name: "طالب جامعي", academic_year: 1, semester: 2 },
      };
    }

    if (!isSupabaseConfigured) {
      // Offline fallback for any email
      const isAd = cleanId.includes("admin");
      const fakeId = `demo-${Date.now()}`;
      return {
        user: { id: fakeId, email: cleanId },
        role: isAd ? "admin" : "student",
        profile: { id: fakeId, display_name: cleanId.split("@")[0] || "مستخدم", academic_year: 1, semester: 1 },
      };
    }

    // Live Supabase Auth
    const emailToUse = cleanId.includes("@") ? cleanId : `${cleanId}@midad.edu`;
    const { data, error } = await supabase.auth.signInWithPassword({
      email: emailToUse,
      password: pass,
    });

    if (error || !data.user) {
      throw new Error(error?.message || "فشل تسجيل الدخول. يرجى التحقق من صحة البيانات.");
    }

    const userId = data.user.id;
    const role = await this.getUserRole(userId);
    const profile = await this.getProfile(userId, data.user.email || cleanId);

    return { user: { id: userId, email: data.user.email || emailToUse }, role, profile };
  },

  async signUp(email: string, pass: string, displayName: string): Promise<{ user: { id: string; email: string }; role: AppRole; profile: Profile }> {
    if (!isSupabaseConfigured) {
      const fakeId = `student-${Date.now()}`;
      return {
        user: { id: fakeId, email },
        role: "student",
        profile: { id: fakeId, display_name: displayName, academic_year: 1, semester: 1 },
      };
    }

    const { data, error } = await supabase.auth.signUp({
      email,
      password: pass,
      options: {
        data: {
          display_name: displayName,
        },
      },
    });

    if (error || !data.user) {
      throw new Error(error?.message || "فشل إنشاء الحساب الجديد.");
    }

    const userId = data.user.id;
    const profile: Profile = {
      id: userId,
      display_name: displayName,
      academic_year: 1,
      semester: 1,
    };

    return { user: { id: userId, email: data.user.email || email }, role: "student", profile };
  },

  async signOut(): Promise<void> {
    if (isSupabaseConfigured) {
      await supabase.auth.signOut();
    }
  },

  async getUserRole(userId: string): Promise<AppRole> {
    if (!isSupabaseConfigured) {
      return "student";
    }

    const { data, error } = await supabase
      .from("user_roles")
      .select("role")
      .eq("user_id", userId)
      .maybeSingle();

    if (error || !data) {
      return "student";
    }

    return (data.role as AppRole) || "student";
  },

  async getProfile(userId: string, fallbackName?: string): Promise<Profile> {
    if (!isSupabaseConfigured) {
      return {
        id: userId,
        display_name: fallbackName || "طالب مِداد",
        academic_year: 1,
        semester: 1,
      };
    }

    const { data, error } = await supabase
      .from("profiles")
      .select("*")
      .eq("id", userId)
      .maybeSingle();

    if (error || !data) {
      return {
        id: userId,
        display_name: fallbackName || "طالب مِداد",
        academic_year: 1,
        semester: 1,
      };
    }

    return data as Profile;
  },

  async updateProfile(userId: string, updates: Partial<Profile>): Promise<void> {
    if (!isSupabaseConfigured) return;
    await supabase.from("profiles").update(updates).eq("id", userId);
  },
};
