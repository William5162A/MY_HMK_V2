// Central curriculum data — single source of truth for all 60 engineering subjects.
// Retains stable IDs, names, academic years, and semesters. All mock prices/descriptions purged.

export type Semester = 1 | 2;
export type Year = 1 | 2 | 3 | 4 | 5;

export interface Subject {
  id: string;
  name: string;
  year: Year;
  semester: Semester;
  order: number;
}

const RAW: Record<string, string[]> = {
  "1-1": [
    "الكيمياء الصناعية",
    "مبادئ عمل الحواسيب",
    "الرسم والتمثيل الهندسي",
    "اللغة الأجنبية 1",
    "الفيزياء",
    "الرياضيات 1",
  ],
  "1-2": [
    "الرياضيات 2",
    "اللغة العربية",
    "أسس الهندسة الكهربائية",
    "البرمجة 2",
    "اللغة الأجنبية 2",
    "الميكانيك الهندسي 1",
  ],
  "2-1": [
    "هندسة الإنتاج",
    "الميكانيك الهندسي 2",
    "اللغة الأجنبية 3",
    "مقاومة المواد وخواصها",
    "البرمجة 2",
    "الرياضيات 3",
  ],
  "2-2": [
    "الرياضيات 4",
    "أسس الهندسة الإلكترونية",
    "نظم التحكم",
    "اللغة الأجنبية 4",
    "أسس الهندسة الحرارية",
    "حساسات ومجسات",
    "ميكانيك الموائع",
  ],
  "3-1": [
    "نظرية الآلات",
    "تقنية البصريات",
    "الشبكات الحاسوبية",
    "القياسات وأجهزة القياس",
    "بنية الحاسوب",
    "الآلات الكهربائية",
  ],
  "3-2": [
    "هندسة الدارات الرقمية",
    "تصميم الأنظمة الميكاترونية",
    "الإلكترونيات الصناعية",
    "التصميم باستخدام الحاسوب",
    "متحكمات منطقية قابلة للبرمجة PLC",
    "تصميم الآلات",
  ],
  "4-1": [
    "القيادة الكهربائية",
    "التحكم الآلي",
    "الصيانة والأمن الصناعي",
    "آلات النقل والرفع",
    "إدارة الجودة",
    "النمذجة والمحاكاة",
  ],
  "4-2": [
    "الأتمتة الصناعية",
    "معالجة الإشارة",
    "متحكمات صغرية",
    "ديناميك آلات واهتزازات",
    "آلات تشكيل المعادن",
    "التدفئة والتكييف",
  ],
  "5-1": [
    "أنظمة الاتصال الصناعية",
    "ترشيد استهلاك الطاقة والبيئة",
    "نظم التحكم في الزمن الحقيقي",
    "نظم القياسات المعلوماتية",
    "تطبيقات صناعية",
  ],
  "5-2": [
    "الذكاء الصنعي",
    "التنظيم الصناعي",
    "الاتصالات",
    "معالجة الصور",
    "تطبيقات PLC في الروبوت",
  ],
};

export const SUBJECTS: Subject[] = Object.entries(RAW).flatMap(([key, names]) => {
  const [y, s] = key.split("-").map(Number) as [Year, Semester];
  return names.map((name, i) => ({
    id: `y${y}s${s}-${String(i + 1).padStart(2, "0")}`,
    name,
    year: y,
    semester: s,
    order: (y * 10 + s) * 100 + i,
  }));
});

export const FEATURED_ID = "y1s2-03";

export const subjectById = (id: string) => SUBJECTS.find((s) => s.id === id);

export const YEAR_NAMES: Record<Year, string> = {
  1: "السنة الأولى",
  2: "السنة الثانية",
  3: "السنة الثالثة",
  4: "السنة الرابعة",
  5: "السنة الخامسة",
};
export const SEM_NAMES: Record<Semester, string> = { 1: "الفصل الأول", 2: "الفصل الثاني" };

/** Current semester followed by every earlier semester, newest first. Never future. */
export function recommend(year: Year, semester: Semester, purchased: Set<string>): Subject[] {
  const rank = (y: number, s: number) => y * 2 + s;
  const cur = rank(year, semester);
  return SUBJECTS.filter((s) => rank(s.year, s.semester) <= cur && !purchased.has(s.id)).sort(
    (a, b) => rank(b.year, b.semester) - rank(a.year, a.semester) || a.order - b.order,
  );
}
