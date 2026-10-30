import { isSupabaseConfigured, supabase } from "../supabase";
import type { PurchaseRecord } from "./types";

// In-memory fallback purchases
const fallbackPurchases: Record<string, Set<string>> = {
  "00000000-0000-0000-0000-000000000002": new Set(["y1s2-03"]), // Student owns electric engineering
};

export const purchaseService = {
  async getUserPurchases(userId: string): Promise<string[]> {
    if (!isSupabaseConfigured) {
      const owned = fallbackPurchases[userId] || fallbackPurchases["00000000-0000-0000-0000-000000000002"] || new Set();
      return Array.from(owned);
    }

    const { data, error } = await supabase
      .from("purchases")
      .select("subject_id")
      .eq("user_id", userId);

    if (error || !data) {
      return [];
    }

    return data.map((p) => p.subject_id);
  },

  async simulatePurchase(userId: string, subjectId: string): Promise<PurchaseRecord> {
    if (!isSupabaseConfigured) {
      if (!fallbackPurchases[userId]) {
        fallbackPurchases[userId] = new Set();
      }
      fallbackPurchases[userId]!.add(subjectId);

      return {
        id: `purchase-${Date.now()}`,
        user_id: userId,
        subject_id: subjectId,
        status: "active",
        created_at: new Date().toISOString(),
      };
    }

    const { data, error } = await supabase
      .from("purchases")
      .insert({
        user_id: userId,
        subject_id: subjectId,
        status: "active",
      })
      .select()
      .single();

    if (error || !data) {
      throw new Error(`فشل تأكيد شراء المادة: ${error?.message || "خطأ غير معروف"}`);
    }

    return data as PurchaseRecord;
  },

  async isPurchased(userId: string, subjectId: string): Promise<boolean> {
    const purchases = await this.getUserPurchases(userId);
    return purchases.includes(subjectId);
  },
};
