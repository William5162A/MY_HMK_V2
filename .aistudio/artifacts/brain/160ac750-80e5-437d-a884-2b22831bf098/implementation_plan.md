# خطة بناء تطبيق "مِداد" (Medad) من الصفر مع البرومبت الهندسي الشامل

هذه الوثيقة تقدم **الخطة المعمارية الشاملة** والبرومبت الهندسي الموحد والمفصل (**One-Shot Master Prompt**) لبناء تطبيق **مِداد** بالكامل من الصفر. تم تصميم هذا الدليل والبرومبت ليمكنك من تقديمه لأي مهندس برمجيات ذكاء اصطناعي لبناء هذا التطبيق المتكامل بدقة متناهية من أمر واحد فقط.

---

## الجزء الأول: الخطة المعمارية والتفصيلية للتطبيق (Architecture & Data Blueprint)

### 1. الرؤية وهوية التطبيق
- **الاسم:** مِداد (Medad)
- **التخصص الأكاديمي:** تطبيق أكاديمي متكامل لقسم **هندسة الميكاترونيك - جامعة حمص**، سوريا.
- **الهدف الأساسي:** منصة مركزية للمواد الدراسية والمحاضرات النظرية والمخبرية وأسئلة الدورات وسلالم التصحيح، مع لوحة إدارة كاملة للمشرف (Admin) لرفع وإدارة الملفات سحابياً عبر Supabase.

### 2. البنية البرمجية والتقنيات (Tech Stack)
- **نظام التشغيل:** Android (Kotlin)
- **واجهة المستخدم:** Jetpack Compose حصراً مع Material Design 3.
- **دعم اللغة والاتجاه:** لغة عربية أصلية بدعم كامل لليمين إلى اليسار (RTL) عبر `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`.
- **إدارة الحالة:** Android ViewModel + StateFlow بنمط معماري أحادي الاتجاه (UDF Architecture).
- **التخزين والسحابة:**
  - تخزين محلي للحالة والتفضيلات وقوائم المواد المضافة والمفضلة (State Persistence / DataStore).
  - تكامل سحابي لرفع الملفات عبر **Supabase Storage** (رفع ملفات PDF، وإرجاع الروابط العامة المباشرة والآمنة للطلاب).
- **نظام الألوان والتصميم (Design Tokens):**
  - اللون الأساسي: الكحلي العميق (`DeepNavy`: `#0F2C59` / `#1E3A8A`)
  - اللون الثانوي التفاعلي: الأخضر النعناعي الزمردي (`EmeraldMint`: `#10B981` / `#059669`)
  - لون هوية الإدارة والمشرف: الذهبي العنبري المشرق (`AdminGold`: `#F59E0B` / `#D97706`)
  - دعم كامل للوضعين الداكن (Dark Mode) والفاتح (Light Mode).

---

### 3. بنك المواد الأكاديمية الكامل (المنهاج الرسمي لجامعة حمص - 60 مادة)
التطبيق يحتوي على **60 مادة دراسية** مقسمة بدقة على 5 سنوات و10 فصول دراسية، **وتبدأ جميع المواد خالية تماماً من البيانات الوهمية** في أقسام المحاضرات وأسئلة الدورات والأسئلة المتوقعة ليتم إضافتها وتغذيتها حصرياً من قبل المشرف:

#### السنة الأولى (12 مادة)
- **الفصل الأول (6 مواد):**
  1. `y1s1-01`: الكيمياء الصناعية
  2. `y1s1-02`: مبادئ عمل الحواسيب
  3. `y1s1-03`: الرسم والتمثيل الهندسي
  4. `y1s1-04`: اللغة الأجنبية 1
  5. `y1s1-05`: الفيزياء
  6. `y1s1-06`: الرياضيات 1
- **الفصل الثاني (6 مواد):**
  1. `y1s2-01`: الرياضيات 2
  2. `y1s2-02`: اللغة العربية
  3. `y1s2-03`: أسس الهندسة الكهربائية
  4. `y1s2-04`: البرمجة 2
  5. `y1s2-05`: اللغة الأجنبية 2
  6. `y1s2-06`: الميكانيك الهندسي 1

#### السنة الثانية (13 مادة)
- **الفصل الأول (6 مواد):**
  1. `y2s1-01`: هندسة الإنتاج
  2. `y2s1-02`: الميكانيك الهندسي 2
  3. `y2s1-03`: اللغة الأجنبية 3
  4. `y2s1-04`: مقاومة المواد وخواصها
  5. `y2s1-05`: البرمجة 2
  6. `y2s1-06`: الرياضيات 3
- **الفصل الثاني (7 مواد):**
  1. `y2s2-01`: الرياضيات 4
  2. `y2s2-02`: أسس الهندسة الإلكترونية
  3. `y2s2-03`: نظم التحكم
  4. `y2s2-04`: اللغة الأجنبية 4
  5. `y2s2-05`: أسس الهندسة الحرارية
  6. `y2s2-06`: حساسات ومجسات
  7. `y2s2-07`: ميكانيك الموائع

#### السنة الثالثة (12 مادة)
- **الفصل الأول (6 مواد):**
  1. `y3s1-01`: نظرية الآلات
  2. `y3s1-02`: تقنية البصريات
  3. `y3s1-03`: الشبكات الحاسوبية
  4. `y3s1-04`: القياسات وأجهزة القياس
  5. `y3s1-05`: بنية الحاسوب
  6. `y3s1-06`: الآلات الكهربائية
- **الفصل الثاني (6 مواد):**
  1. `y3s2-01`: هندسة الدارات الرقمية
  2. `y3s2-02`: تصميم الأنظمة الميكاترونية
  3. `y3s2-03`: الإلكترونيات الصناعية
  4. `y3s2-04`: التصميم باستخدام الحاسوب
  5. `y3s2-05`: متحكمات منطقية قابلة للبرمجة PLC
  6. `y3s2-06`: تصميم الآلات

#### السنة الرابعة (12 مادة)
- **الفصل الأول (6 مواد):**
  1. `y4s1-01`: القيادة الكهربائية
  2. `y4s1-02`: التحكم الآلي
  3. `y4s1-03`: الصيانة والأمن الصناعي
  4. `y4s1-04`: آلات النقل والرفع
  5. `y4s1-05`: إدارة الجودة
  6. `y4s1-06`: النمذجة والمحاكاة
- **الفصل الثاني (6 مواد):**
  1. `y4s2-01`: الأتمتة الصناعية
  2. `y4s2-02`: معالجة الإشارة
  3. `y4s2-03`: متحكمات صغرية
  4. `y4s2-04`: ديناميك آلات واهتزازات
  5. `y4s2-05`: آلات تشكيل المعادن
  6. `y4s2-06`: التدفئة والتكييف

#### السنة الخامسة (11 مادة)
- **الفصل الأول (5 مواد):**
  1. `y5s1-01`: أنظمة الاتصال الصناعية
  2. `y5s1-02`: ترشيد استهلاك الطاقة والبيئة
  3. `y5s1-03`: نظم التحكم في الزمن الحقيقي
  4. `y5s1-04`: نظم القياسات المعلوماتية
  5. `y5s1-05`: تطبيقات صناعية
- **الفصل الثاني (6 مواد):**
  1. `y5s2-01`: الذكاء الصنعي
  2. `y5s2-02`: التنظيم الصناعي
  3. `y5s2-03`: الاتصالات
  4. `y5s2-04`: معالجة الصور
  5. `y5s2-05`: تطبيقات PLC في الروبوت
  6. `y5s2-06`: مشروع التخرج

---

### 4. قواعد تجربة المستخدم وتفاصيل الواجهات الدقيقة

#### أ. شاشة الرئيسية (Home Screen)
- **بطاقة الترحيب (Hero Card):** تصميم انسيابي بتدرج لوني كحلي متطور مع كلمة "مِداد" في الخلفية وعرض شارة "جامعة حمص".
- **شبكة البطاقات الإحصائية الأربع (2x2 Grid):**
  - **بحساب المشرف (Admin Persona):**
    * البطاقة 1 (أعلى اليمين): "عدد المواد المضافة للطلاب" (تُحسب ديناميكياً من المواد المنشورة).
    * البطاقة 2 (أعلى اليسار): "عدد المواد الكلية" (رقم ثابت: 60 مادة).
    * البطاقة 3 (أسفل اليمين): "نسبة اكمال اضافة المواد" (نسبة مئوية محسوبة: المواد المنشورة ÷ 60 × 100%).
    * البطاقة 4 (أسفل اليسار): "أقل سنة في المواد المضافة" (تحليل السنة الدراسية التي تحتوي على أقل نسبة نشر).
    * **قاعدة صارمة:** في حساب المشرف يتم إخفاء أي أقسام أخرى بعد هذه البطاقات الأربع منعاً للازدحام.
  - **بحساب الطالب (Student Persona):**
    * البطاقات الأربع تعرض إحصائيات الطالب: المواد المسجلة، الدروس المنجزة، نسبة إنهاء الدروس، والسنة والفصل الحالي. يتبعها قائمة المقررات المقترحة ومتابعة الدراسة.

#### ب. شاشة الملف الشخصي (Profile Screen)
- **بحساب المشرف (Admin Persona):**
  - صورة المشرف مع وسام الإدارة الذهبي (`AdminGold`).
  - اسم "المشرف العام (admin)" والوصف "مشرف النظام · هندسة الميكاترونيك" **مُوَسّطان تماماً في منتصف الصفحة أفقياً** (`TextAlign.Center` مع `Modifier.fillMaxWidth()`).
  - **حذف وإخفاء تام** لعدد المواد المسجلة، ولا يظهر أي محدد للسنة الدراسية أو الفصل الدراسي.
  - بطاقة إعدادات المظهر (تبديل الوضع الداكن/الفاتح)، وزر تسجيل الخروج/التبديل لحساب الطالب.
- **بحساب الطالب (Student Persona):**
  - اسم الطالب والبريد والرقم الجامعي.
  - إحصائية عدد المواد المسجلة، وأزرار تفاعلية لتحديد السنة الأكاديمية (1-5) والفصل الدراسي (1-2).

#### ج. إدارة وتفاصيل المواد (Subject Detail & Content Management)
- تحتوي كل مادة على 3 أقسام رئيسية:
  1. **شرح المادة والمحاضرات النظرية والعملية:** يبدأ فارغاً بدون أي بيانات وهمية. يتيح للمشرف إضافة دروس وعناوين وملاحظات مفصلة ورفع ملفات PDF.
  2. **أسئلة الدورات:** تبدأ فارغة، وتتيح للمشرف إضافة دورات امتحانية وأسئلة اختيار من متعدد وسلالم تصحيح.
  3. **الأسئلة التي لم ترد:** تبدأ فارغة، ومخصصة لإضافة الأسئلة الامتحانية المتوقعة من قبل المشرف.
- **التكامل السحابي (Supabase Storage):**
  - واجهة لرفع ملفات المحاضرات وسلايدات PDF مباشرة إلى حاوية (Bucket) التخزين السحابي في Supabase.
  - حفظ وتوليد روابط التحميل المباشرة وإتاحتها للطلاب للتحميل والمطالعة الفورية.

---

## الجزء الثاني: البرومبت الهندسي الشامل الموحد (One-Shot Master Prompt)

> **طريقة الاستخدام:** انسخ النص الكامل أدناه وضعه في نافذة المحادثة كأول برومبت عند بناء التطبيق:

```markdown
Build a production-ready, authentic Android application written strictly in Kotlin using Jetpack Compose with Material Design 3 and comprehensive Arabic RTL localization, named "مِداد" (Medad).

The app serves as the official academic platform for Mechatronics Engineering faculty and students at Homs University (جامعة حمص - هندسة الميكاترونيك), Syria.

================================================================================
1. ARCHITECTURAL & CORE REQUIREMENTS
================================================================================
- Architecture: MVVM pattern using Kotlin StateFlow, immutable UI state models, and coroutines.
- UI Framework: Pure Jetpack Compose with Material Design 3, smooth animations, and proper edge-to-edge window insets handling.
- Typography & RTL: Full Arabic RTL layout with `LocalLayoutDirection provides LayoutDirection.Rtl`, using Cairo and Reem Kufi font styling.
- Dual Persona System: Instant role toggle between Student and Admin (المشرف العام).
- Color System:
  * Primary: Deep Navy (`#0F2C59` / `#1E3A8A`)
  * Secondary / Accent: Emerald Mint (`#10B981` / `#059669`)
  * Admin Role Identity: Golden Amber (`#F59E0B` / `#D97706`)
  * Full dark and light theme support with persistent switching.

================================================================================
2. COMPLETE CURRICULUM DATA: EXACT 60 SUBJECTS (جامعة حمص - هندسة الميكاترونيك)
================================================================================
The app MUST contain exactly 60 pre-configured Mechatronics engineering subjects distributed across 5 academic years and 10 semesters.
CRITICAL CONTENT RULE: All subjects MUST initially contain ZERO mock data inside their lecture sections, past exam papers, and unasked questions. They must start completely empty so that the Admin can populate real content manually and upload PDFs.

Here is the exact curriculum list to hardcode into CurriculumData:

[YEAR 1 - السنة الأولى] (Total: 12 subjects)
* Semester 1 (الفصل الأول - 6 subjects):
  1. id: "y1s1-01" | name: "الكيمياء الصناعية"
  2. id: "y1s1-02" | name: "مبادئ عمل الحواسيب"
  3. id: "y1s1-03" | name: "الرسم والتمثيل الهندسي"
  4. id: "y1s1-04" | name: "اللغة الأجنبية 1"
  5. id: "y1s1-05" | name: "الفيزياء"
  6. id: "y1s1-06" | name: "الرياضيات 1"
* Semester 2 (الفصل الثاني - 6 subjects):
  1. id: "y1s2-01" | name: "الرياضيات 2"
  2. id: "y1s2-02" | name: "اللغة العربية"
  3. id: "y1s2-03" | name: "أسس الهندسة الكهربائية"
  4. id: "y1s2-04" | name: "البرمجة 2"
  5. id: "y1s2-05" | name: "اللغة الأجنبية 2"
  6. id: "y1s2-06" | name: "الميكانيك الهندسي 1"

[YEAR 2 - السنة الثانية] (Total: 13 subjects)
* Semester 1 (الفصل الأول - 6 subjects):
  1. id: "y2s1-01" | name: "هندسة الإنتاج"
  2. id: "y2s1-02" | name: "الميكانيك الهندسي 2"
  3. id: "y2s1-03" | name: "اللغة الأجنبية 3"
  4. id: "y2s1-04" | name: "مقاومة المواد وخواصها"
  5. id: "y2s1-05" | name: "البرمجة 2"
  6. id: "y2s1-06" | name: "الرياضيات 3"
* Semester 2 (الفصل الثاني - 7 subjects):
  1. id: "y2s2-01" | name: "الرياضيات 4"
  2. id: "y2s2-02" | name: "أسس الهندسة الإلكترونية"
  3. id: "y2s2-03" | name: "نظم التحكم"
  4. id: "y2s2-04" | name: "اللغة الأجنبية 4"
  5. id: "y2s2-05" | name: "أسس الهندسة الحرارية"
  6. id: "y2s2-06" | name: "حساسات ومجسات"
  7. id: "y2s2-07" | name: "ميكانيك الموائع"

[YEAR 3 - السنة الثالثة] (Total: 12 subjects)
* Semester 1 (الفصل الأول - 6 subjects):
  1. id: "y3s1-01" | name: "نظرية الآلات"
  2. id: "y3s1-02" | name: "تقنية البصريات"
  3. id: "y3s1-03" | name: "الشبكات الحاسوبية"
  4. id: "y3s1-04" | name: "القياسات وأجهزة القياس"
  5. id: "y3s1-05" | name: "بنية الحاسوب"
  6. id: "y3s1-06" | name: "الآلات الكهربائية"
* Semester 2 (الفصل الثاني - 6 subjects):
  1. id: "y3s2-01" | name: "هندسة الدارات الرقمية"
  2. id: "y3s2-02" | name: "تصميم الأنظمة الميكاترونية"
  3. id: "y3s2-03" | name: "الإلكترونيات الصناعية"
  4. id: "y3s2-04" | name: "التصميم باستخدام الحاسوب"
  5. id: "y3s2-05" | name: "متحكمات منطقية قابلة للبرمجة PLC"
  6. id: "y3s2-06" | name: "تصميم الآلات"

[YEAR 4 - السنة الرابعة] (Total: 12 subjects)
* Semester 1 (الفصل الأول - 6 subjects):
  1. id: "y4s1-01" | name: "القيادة الكهربائية"
  2. id: "y4s1-02" | name: "التحكم الآلي"
  3. id: "y4s1-03" | name: "الصيانة والأمن الصناعي"
  4. id: "y4s1-04" | name: "آلات النقل والرفع"
  5. id: "y4s1-05" | name: "إدارة الجودة"
  6. id: "y4s1-06" | name: "النمذجة والمحاكاة"
* Semester 2 (الفصل الثاني - 6 subjects):
  1. id: "y4s2-01" | name: "الأتمتة الصناعية"
  2. id: "y4s2-02" | name: "معالجة الإشارة"
  3. id: "y4s2-03" | name: "متحكمات صغرية"
  4. id: "y4s2-04" | name: "ديناميك آلات واهتزازات"
  5. id: "y4s2-05" | name: "آلات تشكيل المعادن"
  6. id: "y4s2-06" | name: "التدفئة والتكييف"

[YEAR 5 - السنة الخامسة] (Total: 11 subjects)
* Semester 1 (الفصل الأول - 5 subjects):
  1. id: "y5s1-01" | name: "أنظمة الاتصال الصناعية"
  2. id: "y5s1-02" | name: "ترشيد استهلاك الطاقة والبيئة"
  3. id: "y5s1-03" | name: "نظم التحكم في الزمن الحقيقي"
  4. id: "y5s1-04" | name: "نظم القياسات المعلوماتية"
  5. id: "y5s1-05" | name: "تطبيقات صناعية"
* Semester 2 (الفصل الثاني - 6 subjects):
  1. id: "y5s2-01" | name: "الذكاء الصنعي"
  2. id: "y5s2-02" | name: "التنظيم الصناعي"
  3. id: "y5s2-03" | name: "الاتصالات"
  4. id: "y5s2-04" | name: "معالجة الصور"
  5. id: "y5s2-05" | name: "تطبيقات PLC في الروبوت"
  6. id: "y5s2-06" | name: "مشروع التخرج"

TOTAL SUBJECTS COUNT: Exactly 60.

================================================================================
3. SCREEN REQUIREMENTS & UX SPECIFICATIONS
================================================================================

A. HOME SCREEN (الرئيسية):
- Hero Card: Dark navy gradient with watermark "مِداد", greetings, and university badge "جامعة حمص".
- For Student: Shows personal study stats, followed by recommended courses and study tracks.
- For Admin (CRITICAL RESTRICTION):
  * Below the Hero Card, display ONLY the 4 stats cards in a 2x2 grid:
    1. Card 1 (Top-Right): "عدد المواد المضافة للطلاب" (Count of currently published subjects).
    2. Card 2 (Top-Left): "عدد المواد الكلية" (Fixed: 60).
    3. Card 3 (Bottom-Right): "نسبة اكمال اضافة المواد" (Percentage = published / 60 * 100%).
    4. Card 4 (Bottom-Left): "السنة التي فيها اقل عدد من المواد المضافة" (Dynamic year stat).
  * In Admin mode, DO NOT display any other sections after these 4 cards (no recent courses, no schedule, no shortcuts).

B. PROFILE SCREEN (الملف الشخصي):
- For Admin (CRITICAL UX RULES):
  * The Admin title "المشرف العام (admin)" and subtitle "مشرف النظام · هندسة الميكاترونيك" MUST be strictly centered horizontally (`TextAlign.Center` with `Modifier.fillMaxWidth()`).
  * Completely HIDE the student-specific stats row (number of enrolled subjects, current year, current semester).
  * Completely HIDE the year and semester selector buttons.
  * Keep only the admin identity badge, theme switch toggle, and logout/role switch button.
- For Student:
  * Full profile with editable enrolled subjects count, and interactive year (1-5) and semester (1-2) selectors.

C. COURSES BROWSER (تصفح المواد):
- Filter subjects by Academic Year (1 to 5) and Semester (1 or 2).
- Real-time search by subject name.
- For Admin: Quick publish/unpublish toggle button on every subject card.
- For Student: Enroll/unenroll toggle button and subject detail opener.

D. SUBJECT DETAIL SCREEN (تفاصيل المادة):
- Three empty start sections ready for manual data entry and file upload:
  1. "شرح المادة" (Course lectures, topics, summaries, and downloadable PDF files).
  2. "اسئلة الدورات" (Past exam papers with questions, options, and answer keys).
  3. "الأسئلة التي لم ترد" (High-probability exam questions).
- Admin can add new lecture topics, attach PDF file names/links, and publish directly to students.

E. SUPABASE STORAGE CLOUD INTEGRATION:
- Integrated file upload dialog for Admin to upload course materials and PDF files to Supabase Storage.
- Configurable Supabase parameters (Project URL, Anon Key, and Bucket Name).
- Generation of public file URLs for instant download and viewing by students.

================================================================================
4. CODE QUALITY & COMPLETENESS
================================================================================
- Modular Kotlin architecture with dedicated files:
  * `model/CurriculumData.kt`: Contains all 60 subjects and empty getters for topics/exams.
  * `model/Models.kt`: Data classes for UserRole, Subject, TopicItem, ExamPaper, AdminTopic, etc.
  * `viewmodel/AppViewModel.kt`: Centralized state management with StateFlow.
  * `ui/screens/MainSections.kt`: All Compose screens, navigation, and dialogs.
  * `ui/theme/Theme.kt` & `Color.kt`: Exact color tokens and typography.
- All interactive components must have appropriate `Modifier.testTag` identifiers.
- Ensure the app compiles and runs cleanly with zero runtime exceptions.
```

---

## الجزء الثالث: خطوات التنفيذ والمصادقة

1. **مراجعة الخطة والبرومبت المحدّث أعلاه:** يتضمن الآن تفصيلاً كاملاً لجميع المواد الستين (60 مادة) بالاسم والمعرف والسنة والفصل، بالإضافة لكافة الشروط الدقيقة لشاشتي الرئيسية والملف الشخصي والتكامل السحابي.
2. **اعتماد الخطة:** بالضغط على زر **Proceed**، تصبح هذه الخطة والبرومبت المرجع القياسي المعتمد للمشروع في النظام.
