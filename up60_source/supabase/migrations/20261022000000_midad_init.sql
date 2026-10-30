-- ==============================================================================
-- MIDAD (مِداد) — DATABASE SCHEMA & RLS MIGRATIONS
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. User Roles Enum & Table
CREATE TYPE app_role AS ENUM ('student', 'admin');

CREATE TABLE IF NOT EXISTS public.user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role app_role NOT NULL DEFAULT 'student',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT unique_user_role UNIQUE (user_id)
);

-- 3. User Profiles Table
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    academic_year INT NOT NULL DEFAULT 1 CHECK (academic_year BETWEEN 1 AND 5),
    semester INT NOT NULL DEFAULT 1 CHECK (semester IN (1, 2)),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 4. Subjects Table (60 Syrian engineering university subjects)
CREATE TABLE IF NOT EXISTS public.subjects (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    academic_year INT NOT NULL CHECK (academic_year BETWEEN 1 AND 5),
    semester INT NOT NULL CHECK (semester IN (1, 2)),
    is_published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 5. Subject Sections Table (Tab 1 Headings - "شرح المادة")
CREATE TABLE IF NOT EXISTS public.subject_sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id TEXT NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 6. Subject Files Table (PDFs stored in private Supabase Storage bucket)
CREATE TABLE IF NOT EXISTS public.subject_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id UUID NOT NULL REFERENCES public.subject_sections(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    storage_path TEXT NOT NULL,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 7. Purchases Table (Simulated purchases with persistence)
CREATE TABLE IF NOT EXISTS public.purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    subject_id TEXT NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT unique_user_subject_purchase UNIQUE (user_id, subject_id)
);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subject_sections ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subject_files ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchases ENABLE ROW LEVEL SECURITY;

-- Helper function to check if current user is admin
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.user_roles
        WHERE user_id = auth.uid() AND role = 'admin'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Profiles Policies
CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Admins can view all profiles"
    ON public.profiles FOR SELECT
    USING (public.is_admin());

-- User Roles Policies (Client cannot modify roles)
CREATE POLICY "Users can read own role"
    ON public.user_roles FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Admins can view all roles"
    ON public.user_roles FOR SELECT
    USING (public.is_admin());

-- Subjects Policies
-- Students can only view published subjects
CREATE POLICY "Anyone can view published subjects"
    ON public.subjects FOR SELECT
    USING (is_published = true OR public.is_admin());

-- Only Admins have full CRUD on subjects
CREATE POLICY "Admins have full CRUD on subjects"
    ON public.subjects FOR ALL
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- Subject Sections Policies
CREATE POLICY "Admins have full CRUD on sections"
    ON public.subject_sections FOR ALL
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Students can view sections of purchased published subjects"
    ON public.subject_sections FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.subjects s
            JOIN public.purchases p ON p.subject_id = s.id
            WHERE s.id = public.subject_sections.subject_id
              AND s.is_published = true
              AND p.user_id = auth.uid()
        )
    );

-- Subject Files Policies
CREATE POLICY "Admins have full CRUD on files"
    ON public.subject_files FOR ALL
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Students can view files of purchased published subjects"
    ON public.subject_files FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.subject_sections sec
            JOIN public.subjects s ON s.id = sec.subject_id
            JOIN public.purchases p ON p.subject_id = s.id
            WHERE sec.id = public.subject_files.section_id
              AND s.is_published = true
              AND p.user_id = auth.uid()
        )
    );

-- Purchases Policies
CREATE POLICY "Users can view own purchases"
    ON public.purchases FOR SELECT
    USING (auth.uid() = user_id OR public.is_admin());

CREATE POLICY "Students can insert own purchases for published subjects"
    ON public.purchases FOR INSERT
    WITH CHECK (
        auth.uid() = user_id
        AND EXISTS (
            SELECT 1 FROM public.subjects
            WHERE id = subject_id AND is_published = true
        )
    );

-- ==============================================================================
-- AUTOMATIC USER REGISTRATION TRIGGER
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Insert Profile
    INSERT INTO public.profiles (id, display_name, academic_year, semester)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(NEW.email, '@', 1), 'طالب'),
        1,
        1
    );

    -- 2. Insert Default 'student' role (never admin on public signup)
    INSERT INTO public.user_roles (user_id, role)
    VALUES (NEW.id, 'student');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ==============================================================================
-- SECURE ADMIN PROVISIONING FUNCTION (FOR SERVER/SQL SCRIPT)
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.promote_user_to_admin(target_email TEXT)
RETURNS VOID AS $$
DECLARE
    target_user_id UUID;
BEGIN
    SELECT id INTO target_user_id FROM auth.users WHERE email = target_email;
    IF target_user_id IS NOT NULL THEN
        INSERT INTO public.user_roles (user_id, role)
        VALUES (target_user_id, 'admin')
        ON CONFLICT (user_id) DO UPDATE SET role = 'admin';
    ELSE
        RAISE EXCEPTION 'User with email % not found', target_email;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==============================================================================
-- PRIVATE STORAGE BUCKET SETUP SCRIPT
-- ==============================================================================
-- Run in Supabase SQL editor:
-- INSERT INTO storage.buckets (id, name, public) VALUES ('subject-files', 'subject-files', false)
-- ON CONFLICT (id) DO NOTHING;
