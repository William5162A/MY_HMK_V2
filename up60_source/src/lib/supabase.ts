import { createClient } from "@supabase/supabase-js";

// Read from Vite environment variables (.env / .env.example)
const supabaseUrl = import.meta.env["VITE_SUPABASE_URL"] || "https://placeholder-midad.supabase.co";
const supabasePublishableKey = import.meta.env["VITE_SUPABASE_PUBLISHABLE_KEY"] || "placeholder-anon-key";

export const isSupabaseConfigured = Boolean(
  import.meta.env["VITE_SUPABASE_URL"] && 
  import.meta.env["VITE_SUPABASE_PUBLISHABLE_KEY"] &&
  !import.meta.env["VITE_SUPABASE_URL"].includes("placeholder")
);

export const supabase = createClient(supabaseUrl, supabasePublishableKey, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true,
  },
});
