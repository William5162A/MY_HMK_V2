package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.data.SupabaseStorageService
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.ReemKufiFontFamily
import com.example.viewmodel.AppUiState
import com.example.viewmodel.UserRole

// ==============================================================================
// Icon selector matching subject types
// ==============================================================================
fun iconForSubject(name: String): ImageVector {
    return when {
        name.contains("رياضيات") || name.contains("نمذجة") || name.contains("إشارة") -> Icons.Default.Functions
        name.contains("لغة") || name.contains("اللغة") -> Icons.Default.Language
        name.contains("كيمياء") -> Icons.Default.Science
        name.contains("فيزياء") || name.contains("بصريات") -> Icons.Default.Lightbulb
        name.contains("برمجة") || name.contains("حاسوب") || name.contains("حواسيب") || name.contains("متحكمات") -> Icons.Default.Memory
        name.contains("شبكات") || name.contains("اتصال") -> Icons.Default.Lan
        name.contains("الاتصالات") -> Icons.Default.CellTower
        name.contains("كهربائية") || name.contains("القيادة") || name.contains("إلكترون") -> Icons.Default.Bolt
        name.contains("حرارية") || name.contains("تدفئة") || name.contains("طاقة") -> Icons.Default.LocalFireDepartment
        name.contains("قياس") || name.contains("حساسات") || name.contains("تحكم") -> Icons.Default.Speed
        name.contains("رسم") || name.contains("تصميم") -> Icons.Default.Brush
        name.contains("روبوت") || name.contains("ذكاء") || name.contains("PLC") || name.contains("أتمتة") -> Icons.Default.SmartToy
        name.contains("صور") -> Icons.Default.Visibility
        name.contains("آلات") || name.contains("ميكانيك") || name.contains("إنتاج") || name.contains("مواد") || name.contains("معادن") -> Icons.Default.Settings
        else -> Icons.AutoMirrored.Filled.MenuBook
    }
}

// ==============================================================================
// 1. الرئيسية (Home Section)
// ==============================================================================
@Composable
fun HomeScreen(
    state: AppUiState,
    onNavigateToBrowse: () -> Unit,
    onNavigateToMyCourses: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectSubject: (String) -> Unit
) {
    val enrolledSubjects = remember(state.enrolledSubjectIds) {
        CurriculumData.ALL_SUBJECTS.filter { state.enrolledSubjectIds.contains(it.id) }
    }
    val recommendedSubjects = remember(state.academicYear, state.semester, state.enrolledSubjectIds) {
        CurriculumData.ALL_SUBJECTS.filter {
            it.year == state.academicYear && it.semester == state.semester && !state.enrolledSubjectIds.contains(it.id)
        }
    }
    val displayName = if (state.role == UserRole.ADMIN) "المشرف العام" else "طالب"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_home"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --------------------------------------------------------------------------
        // 1. Hero Welcome Card (Preserved completely as requested)
        // --------------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF111A3A),
                                    Color(0xFF0B132B)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Text(
                        text = "مِداد",
                        fontSize = 96.sp,
                        fontFamily = ReemKufiFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.045f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-12).dp, y = 24.dp)
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.role != UserRole.ADMIN) {
                                Text(
                                    text = "${CurriculumData.getYearName(state.academicYear)} · ${CurriculumData.getSemesterName(state.semester)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFF94A3B8)
                                )
                            } else {
                                Text(
                                    text = "لوحة تحكم المشرف",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = EmeraldMint
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldMint.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "جامعة حمص",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = EmeraldMint,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "أهلًا بعودتك، $displayName",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = ReemKufiFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (state.role == UserRole.ADMIN)
                                "أنت في وضع المشرف العام: يمكنك نشر المقررات وإضافة العناوين ورفع ملفات الـ PDF."
                            else
                                "رحلتك نحو التفوق الأكاديمي تبدأ بخطوة صغيرة اليوم.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 21.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateToBrowse,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldMint,
                                    contentColor = DeepNavy
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = if (state.role == UserRole.ADMIN) "إدارة وتصفح المواد" else "تصفح المواد المنشورة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (state.role != UserRole.ADMIN) {
                                OutlinedButton(
                                    onClick = onNavigateToMyCourses,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFF94A3B8).copy(alpha = 0.35f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
                                ) {
                                    Text(
                                        text = "موادي الدراسية",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------------
        // 2. The 4 Stats Cards Grid (2x2):
        // Card 0 (Top-Right in RTL): "المواد المشتراة" (Purchased subjects)
        // Card 1 (Top-Left in RTL): "الدروس المنجزة" (Completed topics ONLY from purchased subjects / Total topics of purchased subjects)
        // Card 2 (Bottom-Right in RTL): "نسبة إنهاء الدروس" (Progress percentage of purchased subjects)
        // Card 3 (Bottom-Left in RTL): "السنة الدراسية والفصل الحالي" (Academic year & semester)
        // --------------------------------------------------------------------------
        item {
            if (state.role == UserRole.ADMIN) {
                // ======================================================================
                // Admin 4 Stats Cards Grid (2x2):
                // Card 0 (Top-Right in RTL): عدد المواد المضافة للطلاب
                // Card 1 (Top-Left in RTL): عدد المواد الكلية اي 60
                // Card 2 (Bottom-Right in RTL): نسبة اكمال اضافة المواد
                // Card 3 (Bottom-Left in RTL): السنة التي فيها اقل عدد من المواد المضافة
                // ======================================================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 0 (Top-Right in RTL): عدد المواد المضافة للطلاب
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToBrowse() }
                                .testTag("home_stat_admin_added_subjects"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldMint),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = DeepNavy.copy(alpha = 0.75f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "${state.addedSubjectsCount}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = DeepNavy
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "عدد المواد المضافة للطلاب",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepNavy.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 1 (Top-Left in RTL): عدد المواد الكلية اي 60
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToBrowse() }
                                .testTag("home_stat_admin_total_subjects"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "60",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "عدد المواد الكلية",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 2 (Bottom-Right in RTL): نسبة اكمال اضافة المواد
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToBrowse() }
                                .testTag("home_stat_admin_completion_percentage"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "${state.addedCompletionPercentage}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "نسبة اكمال اضافة المواد",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 3 (Bottom-Left in RTL): السنة التي فيها اقل عدد من المواد المضافة
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToBrowse() }
                                .testTag("home_stat_admin_lowest_year"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = CurriculumData.getYearName(state.yearWithLowestAddedSubjects.year),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "أقل سنة في المواد المضافة (${state.yearWithLowestAddedSubjects.count})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                // ======================================================================
                // Student 4 Stats Cards Grid (2x2):
                // Card 0 (Top-Right in RTL): "المواد المشتراة" (Purchased subjects)
                // Card 1 (Top-Left in RTL): "الدروس المنجزة"
                // Card 2 (Bottom-Right in RTL): "نسبة إنهاء الدروس"
                // Card 3 (Bottom-Left in RTL): "السنة الدراسية والفصل الحالي"
                // ======================================================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 0 (Top-Right): Purchased Subjects Count (المواد المشتراة)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToMyCourses() }
                                .testTag("home_stat_purchased_subjects"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldMint),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = DeepNavy.copy(alpha = 0.75f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "${enrolledSubjects.size}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = DeepNavy
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "المواد المشتراة",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepNavy.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Card 1 (Top-Left): Completed topics ONLY from purchased subjects
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToMyCourses() }
                                .testTag("home_stat_completed_lessons"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "${state.purchasedCompletedTopicsCount} / ${state.purchasedTotalTopicsCount}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "الدروس المنجزة",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 2 (Bottom-Right): Completion Percentage of purchased subjects
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToMyCourses() }
                                .testTag("home_stat_progress_percentage"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "${state.purchasedProgressPercentage}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "نسبة إنهاء الدروس",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Card 3 (Bottom-Left): Academic Year & Current Semester
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToProfile() }
                                .testTag("home_stat_academic_info"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = CurriculumData.getYearName(state.academicYear),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = CurriculumData.getSemesterName(state.semester),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = EmeraldMint
                                )
                            }
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------------
        // 3. Enrolled & Recommended Subjects Sections (STUDENTS ONLY):
        // For Admin: All sections after the 4 cards are removed as requested.
        // --------------------------------------------------------------------------
        if (state.role != UserRole.ADMIN) {
            if (enrolledSubjects.isNotEmpty()) {
                item {
                    SectionTitle(
                        kicker = "من حيث توقفت",
                        title = "تابع الدراسة"
                    )
                }

                items(enrolledSubjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        isEnrolled = true,
                        role = state.role,
                        isPublished = state.addedSubjectIds.contains(subject.id),
                        onActionClick = { onSelectSubject(subject.id) }
                    )
                }
            }

            item {
                SectionTitle(
                    kicker = "حسب سنتك وفصلك (${CurriculumData.getYearName(state.academicYear)} · ${CurriculumData.getSemesterName(state.semester)})",
                    title = "مواد مقترحة لدراستها",
                    actionLabel = "تغيير الفصل",
                    onActionClick = onNavigateToProfile
                )
            }

            if (recommendedSubjects.isNotEmpty()) {
                items(recommendedSubjects.take(6), key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        isEnrolled = false,
                        role = state.role,
                        isPublished = state.addedSubjectIds.contains(subject.id),
                        onActionClick = { onNavigateToBrowse() }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "لم يتم نشر مواد إضافية في هذا الفصل بعد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================================
// SectionTitle matching Bits.tsx
// ==============================================================================
@Composable
fun SectionTitle(
    kicker: String? = null,
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (!kicker.isNullOrEmpty()) {
                Text(
                    text = kicker,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = ReemKufiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (!actionLabel.isNullOrEmpty() && onActionClick != null) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onActionClick() }
                    .padding(4.dp)
            )
        }
    }
}

// ==============================================================================
// SubjectCard
// ==============================================================================
@Composable
fun SubjectCard(
    subject: Subject,
    isEnrolled: Boolean,
    role: UserRole,
    isPublished: Boolean = false,
    onActionClick: () -> Unit,
    onTogglePublish: (() -> Unit)? = null
) {
    val icon = remember(subject.name) { iconForSubject(subject.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldMint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EmeraldMint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = String.format("%02d", subject.year),
                    fontFamily = ReemKufiFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${CurriculumData.getYearName(subject.year)} · ${CurriculumData.getSemesterName(subject.semester)}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dedicated Red/Green Toggle for Admin
            if (role == UserRole.ADMIN) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ظهور المادة للطلاب:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        onClick = { onTogglePublish?.invoke() },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPublished) Color(0xFF10B981) else Color(0xFFEF4444),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("toggle_subject_${subject.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isPublished) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Text(
                                text = if (isPublished) "مضافة للطلاب (مفعلة)" else "غير مضافة (اضغط للتفعيل)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == UserRole.ADMIN) {
                    Text(
                        text = if (isPublished) "مدرجة في قائمة الطلاب" else "قيد الإعداد (مسودة)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isPublished) Color(0xFF059669) else Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                } else if (isEnrolled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "مادة مشتراة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = EmeraldMint
                        )
                    }
                } else {
                    Text(
                        text = "مقرر دراسي معتمد",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    color = DeepNavy,
                    contentColor = Color.White
                ) {
                    Text(
                        text = if (role == UserRole.ADMIN) "إدارة المحتوى والشرح" else if (isEnrolled) "متابعة الدراسة" else "تصفح المادة",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ==============================================================================
// 2. موادي / المواد المضافة (My Courses / Added Courses Section)
// ==============================================================================
@Composable
fun MyCoursesScreen(
    state: AppUiState,
    onSelectSubject: (String) -> Unit,
    onNavigateToBrowse: () -> Unit,
    onTogglePublishSubject: ((String) -> Unit)? = null
) {
    val isAdmin = state.role == UserRole.ADMIN
    val subjectList = remember(isAdmin, state.enrolledSubjectIds, state.addedSubjectIds) {
        if (isAdmin) {
            CurriculumData.ALL_SUBJECTS.filter { state.addedSubjectIds.contains(it.id) }
        } else {
            CurriculumData.ALL_SUBJECTS.filter { state.enrolledSubjectIds.contains(it.id) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_my_courses"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = if (isAdmin) "لوحة تحكم المشرف · جامعة حمص" else "مكتبتك الأكاديمية",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isAdmin) "المواد المضافة" else "موادي الدراسية",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = ReemKufiFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isAdmin) {
                        "${subjectList.size} مادة مفعلة ومضافة للطلاب من أصل ${CurriculumData.TOTAL_SUBJECTS_COUNT}"
                    } else {
                        "${subjectList.size} مادة مسجلة ومفتوحة في حسابك بجامعة حمص"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (subjectList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isAdmin) Icons.Default.PostAdd else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isAdmin) "لم تقم بإضافة أي مادة بعد" else "لم تسجل أي مادة بعد",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isAdmin) {
                                "عدد المواد المضافة حالياً 0. يمكنك الانتقال إلى قسم 'كل المواد' وتفعيل زر الإضافة الأخضر لأي مادة لتظهر هنا وللطلاب."
                            } else {
                                "تصفح مقررات الخطة الدراسية لجامعة حمص وسجل في مواد فصلك الحالي."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onNavigateToBrowse,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepNavy,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (isAdmin) "الذهاب إلى كل المواد وتفعيل المواد" else "تصفح المواد لإضافتها",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(subjectList, key = { it.id }) { subject ->
                SubjectCard(
                    subject = subject,
                    isEnrolled = !isAdmin,
                    role = state.role,
                    isPublished = if (isAdmin) true else state.addedSubjectIds.contains(subject.id),
                    onActionClick = { onSelectSubject(subject.id) },
                    onTogglePublish = { onTogglePublishSubject?.invoke(subject.id) }
                )
            }
        }
    }
}

// ==============================================================================
// 3. SubjectDetailScreen: Detail View with the 3 Sections
// (شرح المادة ، اسئلة الدورات ، الاسئلة التي لم ترد)
// ==============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subject: Subject,
    state: AppUiState,
    onBack: () -> Unit,
    onToggleTopicCompletion: (String) -> Unit,
    onAddCustomTopic: ((subjectId: String, title: String, description: String, attachments: List<TopicAttachment>) -> Unit)? = null,
    onRemoveCustomTopic: ((subjectId: String, topicId: String) -> Unit)? = null,
    onUpdateSupabaseConfig: ((SupabaseConfig) -> Unit)? = null
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("شرح المادة", "اسئلة الدورات", "الاسئلة التي لم ترد")

    val topics = remember(subject.id) { CurriculumData.getTopicsForSubject(subject.id) }
    val examPapers = remember(subject.id) { CurriculumData.getExamPapersForSubject(subject.id) }
    val unaskedQuestions = remember(subject.id) { CurriculumData.getUnaskedQuestionsForSubject(subject.id) }
    val customTopics = remember(state.customTopicsBySubject, subject.id) {
        state.customTopicsBySubject[subject.id].orEmpty()
    }

    val completedCount = remember(topics, customTopics, state.completedTopicIds) {
        val standardCount = topics.count { state.completedTopicIds.contains(it.id) }
        val customCount = customTopics.count { state.completedTopicIds.contains(it.id) }
        if (customTopics.isNotEmpty()) customCount else standardCount
    }
    val totalCount = if (customTopics.isNotEmpty()) customTopics.size else topics.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val isAdmin = state.role == UserRole.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${CurriculumData.getYearName(subject.year)} · ${CurriculumData.getSemesterName(subject.semester)} · جامعة حمص",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAdmin) Color(0xFF10B981).copy(alpha = 0.15f) else EmeraldMint.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAdmin) Color(0xFF10B981).copy(alpha = 0.35f) else EmeraldMint.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "لوحة المشرف" else "مادة مشتراة",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = if (isAdmin) Color(0xFF059669) else EmeraldMint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Subject Progress Summary Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAdmin) "حالة محتوى المقرر الأكاديمي" else "تقدم إنجاز المادة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAdmin) {
                                "${customTopics.size} عناوين ومحاضرات مرفوعة"
                            } else {
                                "$completedCount من $totalCount دروس منجزة (${(progress * 100).toInt()}%)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldMint
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { if (isAdmin) (if (customTopics.isNotEmpty()) 1f else 0f) else progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldMint,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // The 3 Section Tabs (شرح المادة ، اسئلة الدورات ، الاسئلة التي لم ترد)
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> CourseExplanationSection(
                    subjectId = subject.id,
                    subjectName = subject.name,
                    role = state.role,
                    curriculumTopics = topics,
                    customTopics = customTopics,
                    completedTopicIds = state.completedTopicIds,
                    supabaseConfig = state.supabaseConfig,
                    onToggleTopicCompletion = onToggleTopicCompletion,
                    onAddCustomTopic = onAddCustomTopic,
                    onRemoveCustomTopic = onRemoveCustomTopic,
                    onUpdateSupabaseConfig = onUpdateSupabaseConfig
                )
                1 -> PastExamsSection(examPapers = examPapers)
                2 -> UnaskedQuestionsSection(questions = unaskedQuestions)
            }
        }
    }
}

// ------------------------------------------------------------------------------
// Section 1: شرح المادة (مع دعم إضافة عنوان ورفع ملفات Supabase للمشرف)
// ------------------------------------------------------------------------------
@Composable
fun CourseExplanationSection(
    subjectId: String,
    subjectName: String,
    role: UserRole,
    curriculumTopics: List<TopicItem>,
    customTopics: List<AdminTopic>,
    completedTopicIds: Set<String>,
    supabaseConfig: SupabaseConfig,
    onToggleTopicCompletion: (String) -> Unit,
    onAddCustomTopic: ((subjectId: String, title: String, description: String, attachments: List<TopicAttachment>) -> Unit)?,
    onRemoveCustomTopic: ((subjectId: String, topicId: String) -> Unit)?,
    onUpdateSupabaseConfig: ((SupabaseConfig) -> Unit)?
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    val isAdmin = role == UserRole.ADMIN

    if (showAddDialog && onAddCustomTopic != null) {
        AddTopicWithFileDialog(
            subjectId = subjectId,
            supabaseConfig = supabaseConfig,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, attachments ->
                onAddCustomTopic(subjectId, title, desc, attachments)
                showAddDialog = false
            }
        )
    }

    if (showConfigDialog && onUpdateSupabaseConfig != null) {
        SupabaseConfigDialog(
            currentConfig = supabaseConfig,
            onDismiss = { showConfigDialog = false },
            onSave = { updated ->
                onUpdateSupabaseConfig(updated)
                showConfigDialog = false
                Toast.makeText(context, "تم حفظ إعدادات Supabase بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("section_course_explanation"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Top Action Bar: Add Title & Config
        if (isAdmin) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("btn_add_topic_title")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة عنوان", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }

                    OutlinedButton(
                        onClick = { showConfigDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = EmeraldMint
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعدادات Supabase", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Content Display
        if (isAdmin) {
            // For Admin: Starts empty if no titles added yet!
            if (customTopics.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldMint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "قسم شرح المادة فارغ حالياً",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "لم يتم إضافة أي عنوان بعد. اضغط على زر 'إضافة عنوان' أعلاه لكتابة عنوان درس جديد وتحديد ملف الـ PDF من هاتفك لرفعه على Supabase ليظهر للطلاب.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldMint,
                                    contentColor = DeepNavy
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة عنوان الآن", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "العناوين والمحاضرات المرفوعة (${customTopics.size} عناوين):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(customTopics, key = { it.id }) { adminTopic ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = adminTopic.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (adminTopic.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = adminTopic.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onRemoveCustomTopic?.invoke(subjectId, adminTopic.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف العنوان",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (adminTopic.attachments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "الملفات المرفوعة على Supabase:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                adminTopic.attachments.forEach { file ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PictureAsPdf,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = file.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${file.sizeFormatted} · ${file.uploadDate}",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { openAttachment(context, file) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DeepNavy,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("فتح الملف", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // For Student: Displays the Admin-added titles with uploaded files!
            if (customTopics.isNotEmpty()) {
                item {
                    Text(
                        text = "محاضرات ودروس المقرر المرفوعة من قِبل المشرف:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(customTopics, key = { it.id }) { adminTopic ->
                    val isDone = completedTopicIds.contains(adminTopic.id)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDone) EmeraldMint.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isDone,
                                    onCheckedChange = { onToggleTopicCompletion(adminTopic.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = EmeraldMint,
                                        checkmarkColor = DeepNavy
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = adminTopic.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (adminTopic.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = adminTopic.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (adminTopic.attachments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                adminTopic.attachments.forEach { file ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PictureAsPdf,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = file.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${file.sizeFormatted} · مرفوع على السحابة",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { openAttachment(context, file) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DeepNavy,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تحميل / فتح", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // If admin hasn't added custom topics yet, display clean empty state
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldMint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "لم تتم إضافة دروس أو محاضرات بعد",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "يقوم المشرف حالياً بإعداد المحتوى وتجهيز ملفات الشرح لهذا المقرر، وسيتم إتاحتها هنا فور رفعها.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// Dialog for Admin to Add New Topic Title and Upload PDF to Supabase
// ------------------------------------------------------------------------------
@Composable
fun AddTopicWithFileDialog(
    subjectId: String,
    supabaseConfig: SupabaseConfig,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, attachments: List<TopicAttachment>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            selectedFileName = SupabaseStorageService.getFileName(context, uri)
            selectedFileSize = try {
                val stream = context.contentResolver.openInputStream(uri)
                val len = stream?.available()?.toLong() ?: 0L
                SupabaseStorageService.formatFileSize(len)
            } catch (_: Exception) {
                "PDF"
            }
            errorMessage = null
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldMint)
                Text(
                    text = "إضافة عنوان ورفع ملف للمادة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "أدخل عنوان الدرس أو الوحدة، وحدد ملف الـ PDF من هاتفك ليتم رفعه إلى Supabase:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الدرس / المحاضرة *") },
                    placeholder = { Text("مثال: المحاضرة 1: مدخل ومفاهيم أساسية") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_topic_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف أو ملخص مختصر (اختياري)") },
                    placeholder = { Text("ملاحظات للطلاب حول محتوى الملف...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                // File Attachment Section
                Text(
                    text = "الملف المرفق (PDF):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedUri == null) {
                    OutlinedButton(
                        onClick = { filePicker.launch("application/pdf") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_select_file"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة ملف (تحديد PDF من الهاتف)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMint)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444)
                                )
                                Column {
                                    Text(
                                        text = selectedFileName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = selectedFileSize,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { filePicker.launch("application/pdf") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "تغيير الملف", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (isUploading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = EmeraldMint
                        )
                        Text(
                            text = "جاري رفع الملف إلى Supabase Storage...",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldMint
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "يرجى كتابة عنوان للدرس أو المحاضرة"
                        return@Button
                    }
                    if (selectedUri == null) {
                        errorMessage = "يرجى الضغط على زر 'إضافة ملف' وتحديد ملف PDF من هاتفك"
                        return@Button
                    }

                    isUploading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = SupabaseStorageService.uploadFile(
                            context = context,
                            fileUri = selectedUri!!,
                            fileName = selectedFileName,
                            subjectId = subjectId,
                            config = supabaseConfig
                        )
                        isUploading = false
                        result.onSuccess { attachment ->
                            onConfirm(title, description, listOf(attachment))
                        }.onFailure { err ->
                            errorMessage = err.message ?: "حدث خطأ أثناء الرفع"
                        }
                    }
                },
                enabled = !isUploading && title.isNotBlank() && selectedUri != null,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = Color.White),
                modifier = Modifier.testTag("btn_confirm_upload_topic")
            ) {
                Text("رفع إلى Supabase وحفظ العنوان", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("إلغاء")
            }
        }
    )
}

// ------------------------------------------------------------------------------
// Dialog for Supabase Storage Configuration
// ------------------------------------------------------------------------------
@Composable
fun SupabaseConfigDialog(
    currentConfig: SupabaseConfig,
    onDismiss: () -> Unit,
    onSave: (SupabaseConfig) -> Unit
) {
    var url by remember { mutableStateOf(currentConfig.projectUrl) }
    var anonKey by remember { mutableStateOf(currentConfig.anonKey) }
    var bucket by remember { mutableStateOf(currentConfig.bucketName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = EmeraldMint)
                Text("إعدادات Supabase Storage", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "يمكنك ربط مشروع Supabase الخاص بك لرفع الملفات والوصول إليها عبر السحابة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Project URL") },
                    placeholder = { Text("https://xyzcompany.supabase.co") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = anonKey,
                    onValueChange = { anonKey = it },
                    label = { Text("Anon Key (Public)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = bucket,
                    onValueChange = { bucket = it },
                    label = { Text("Storage Bucket Name") },
                    placeholder = { Text("subject-files") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(SupabaseConfig(projectUrl = url.trim(), anonKey = anonKey.trim(), bucketName = bucket.trim().ifBlank { "subject-files" }))
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = Color.White)
            ) {
                Text("حفظ الإعدادات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

fun openAttachment(context: android.content.Context, attachment: TopicAttachment) {
    try {
        val uri = if (attachment.localUri != null) {
            Uri.parse(attachment.localUri)
        } else {
            Uri.parse(attachment.supabaseUrl)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.supabaseUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح الملف: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

// ------------------------------------------------------------------------------
// Section 2: اسئلة الدورات
// ------------------------------------------------------------------------------
@Composable
fun PastExamsSection(
    examPapers: List<ExamPaper>
) {
    if (examPapers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(EmeraldMint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = EmeraldMint,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "قسم أسئلة الدورات فارغ حالياً",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "لا توجد أسئلة دورات مضافة لهذه المادة حالياً. سيتم إضافتها لاحقاً.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    var selectedPaperIndex by remember { mutableIntStateOf(0) }
    val currentPaper = examPapers.getOrNull(selectedPaperIndex) ?: examPapers.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exam Session Selector Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "اختر دورة امتحانية لدراسة وحل أسئلتها:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(examPapers.indices.toList()) { index ->
                        val paper = examPapers[index]
                        val isSelected = selectedPaperIndex == index
                        Surface(
                            onClick = { selectedPaperIndex = index },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) DeepNavy else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) DeepNavy else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = paper.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (currentPaper != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentPaper.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "المدة: ${currentPaper.durationMinutes} دقيقة · إجمالي ${currentPaper.questionsCount} سؤال مؤتمت",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldMint.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "دورة رسمية",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldMint,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            items(currentPaper.questions, key = { it.id }) { question ->
                ExamQuestionCard(question = question)
            }
        }
    }
}

@Composable
fun ExamQuestionCard(question: ExamQuestion) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepNavy,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "السؤال ${question.questionNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (selectedOption != null) {
                    val isCorrect = selectedOption == question.correctIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCorrect) EmeraldMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isCorrect) "إجابة صحيحة ✓" else "إجابة خاطئة ✗",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCorrect) EmeraldMint else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Options (A, B, C, D)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedOption == optIndex
                    val isCorrect = optIndex == question.correctIndex
                    val showResult = selectedOption != null

                    val bgColor = when {
                        !showResult -> if (isSelected) DeepNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        isSelected && isCorrect -> EmeraldMint.copy(alpha = 0.2f)
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        isCorrect -> EmeraldMint.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    val borderColor = when {
                        !showResult -> if (isSelected) DeepNavy else MaterialTheme.colorScheme.outlineVariant
                        isSelected && isCorrect -> EmeraldMint
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                        isCorrect -> EmeraldMint.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }

                    Surface(
                        onClick = {
                            selectedOption = optIndex
                            showExplanation = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('أ'.code + optIndex).toChar()})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedOption == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Explanation section
            if (showExplanation) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldMint, modifier = Modifier.size(16.dp))
                            Text(
                                text = "توضيح وشرح الحل النموذجي:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// Section 3: الأسئلة التي لم ترد
// ------------------------------------------------------------------------------
@Composable
fun UnaskedQuestionsSection(
    questions: List<UnaskedQuestion>
) {
    if (questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "قسم الأسئلة التي لم ترد فارغ حالياً",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "لا توجد أسئلة متوقعة أو غير واردة مضافة لهذه المادة حالياً. سيتم إضافتها لاحقاً.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EmeraldMint.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldMint,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "بنك الأسئلة المتوقعة (لم ترد سابقاً)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "أسئلة مركزة أعدها أساتذة جامعة حمص تغطي ثغرات الدورات السابقة ومرشحة للاختبارات القادمة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(questions, key = { it.id }) { q ->
            UnaskedQuestionCard(q = q)
        }
    }
}

@Composable
fun UnaskedQuestionCard(q: UnaskedQuestion) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = q.priority,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = q.topicName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = q.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = q.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                q.options.forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedOption == optIndex
                    val isCorrect = optIndex == q.correctIndex
                    val showResult = selectedOption != null

                    val bgColor = when {
                        !showResult -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        isSelected && isCorrect -> EmeraldMint.copy(alpha = 0.2f)
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        isCorrect -> EmeraldMint.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    Surface(
                        onClick = {
                            selectedOption = optIndex
                            showExplanation = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showResult && isCorrect) EmeraldMint else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('أ'.code + optIndex).toChar()})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (showExplanation) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "سبب التوقع وتوضيح الحل:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldMint
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.explanation,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 4. تصفح المواد / كل المواد (Browse Courses / All Courses Section)
// ==============================================================================
@Composable
fun BrowseCoursesScreen(
    state: AppUiState,
    onSearchChange: (String) -> Unit,
    onFilterYearChange: (Int?) -> Unit,
    onFilterSemesterChange: (Int?) -> Unit,
    onToggleEnroll: (String) -> Unit,
    onTogglePublishSubject: ((String) -> Unit)? = null,
    onSelectSubject: ((String) -> Unit)? = null
) {
    val isAdmin = state.role == UserRole.ADMIN
    val filteredList = remember(state.searchQuery, state.filterYear, state.filterSemester) {
        CurriculumData.ALL_SUBJECTS.filter { subject ->
            val matchQuery = state.searchQuery.isBlank() || subject.name.contains(state.searchQuery.trim(), ignoreCase = true)
            val matchYear = state.filterYear == null || subject.year == state.filterYear
            val matchSem = state.filterSemester == null || subject.semester == state.filterSemester
            matchQuery && matchYear && matchSem
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_browse"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (isAdmin) "لوحة تحكم المشرف · جامعة حمص" else "الخطة الدراسية الكاملة · جامعة حمص",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isAdmin) "كل المواد" else "تصفح المواد",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = ReemKufiFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isAdmin) {
                        "تفعيل ظهور المواد لحسابات الطلاب من خلال زر التفعيل الأخضر، والضغط على المادة لإدارتها ورفع ملفاتها."
                    } else {
                        "جميع مقررات الخطة الدراسية مرتبة حسب السنة والفصل."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_subject_input"),
                placeholder = { Text("ابحث عن مادة…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldMint,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "تصفية حسب السنة الدراسية:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterPill(
                            selected = state.filterYear == null,
                            label = "كل السنوات",
                            onClick = { onFilterYearChange(null) }
                        )
                    }
                    items((1..5).toList()) { yr ->
                        FilterPill(
                            selected = state.filterYear == yr,
                            label = CurriculumData.getYearName(yr),
                            onClick = { onFilterYearChange(if (state.filterYear == yr) null else yr) }
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "تصفية حسب الفصل:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterPill(
                        selected = state.filterSemester == null,
                        label = "كل الفصول",
                        onClick = { onFilterSemesterChange(null) }
                    )
                    FilterPill(
                        selected = state.filterSemester == 1,
                        label = "الفصل الأول",
                        onClick = { onFilterSemesterChange(if (state.filterSemester == 1) null else 1) }
                    )
                    FilterPill(
                        selected = state.filterSemester == 2,
                        label = "الفصل الثاني",
                        onClick = { onFilterSemesterChange(if (state.filterSemester == 2) null else 2) }
                    )
                }
            }
        }

        item {
            Text(
                text = "${filteredList.size} مادة معروضة",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredList, key = { it.id }) { subject ->
            SubjectCard(
                subject = subject,
                isEnrolled = if (isAdmin) false else state.enrolledSubjectIds.contains(subject.id),
                role = state.role,
                isPublished = state.addedSubjectIds.contains(subject.id),
                onActionClick = {
                    if (isAdmin) {
                        onSelectSubject?.invoke(subject.id)
                    } else {
                        onToggleEnroll(subject.id)
                    }
                },
                onTogglePublish = {
                    onTogglePublishSubject?.invoke(subject.id)
                }
            )
        }
    }
}

// ==============================================================================
// 5. ملفي الشخصي (Profile Section)
// ==============================================================================
@Composable
fun ProfileScreen(
    state: AppUiState,
    onToggleDarkMode: () -> Unit,
    onYearChange: (Int) -> Unit,
    onSemesterChange: (Int) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_profile"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.role == UserRole.ADMIN) Color(0xFFF59E0B).copy(alpha = 0.18f)
                                else EmeraldMint.copy(alpha = 0.18f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.role == UserRole.ADMIN) "أ" else "ط",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.role == UserRole.ADMIN) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (state.role == UserRole.ADMIN) "المشرف العام (admin)" else "طالب جامعي (user)",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (state.role == UserRole.ADMIN) "مشرف النظام · هندسة الميكاترونيك" else "طالب هندسة ميكاترونيك · جامعة حمص",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (state.role == UserRole.ADMIN) Color(0xFFF59E0B).copy(alpha = 0.15f)
                        else EmeraldMint.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (state.role == UserRole.ADMIN) Color(0xFFF59E0B).copy(alpha = 0.4f)
                            else EmeraldMint.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (state.role == UserRole.ADMIN) Icons.Default.Shield else Icons.Default.School,
                                contentDescription = null,
                                tint = if (state.role == UserRole.ADMIN) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.role == UserRole.ADMIN) "نوع الحساب: مسؤول (Admin)" else "نوع الحساب: طالب (Student)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (state.role == UserRole.ADMIN) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (state.role != UserRole.ADMIN) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${state.enrolledSubjectIds.size}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("مادة مسجلة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = CurriculumData.getYearName(state.academicYear).replace("السنة ", ""),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("السنة الدراسية", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = CurriculumData.getSemesterName(state.semester).replace("الفصل ", ""),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("الفصل الحالي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (state.role != UserRole.ADMIN) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "السنة الدراسية",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تُبنى المواد المقترحة على سنتك وفصلك الحاليين في جامعة حمص.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..5).forEach { yr ->
                                    val selected = state.academicYear == yr
                                    Button(
                                        onClick = { onYearChange(yr) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selected) DeepNavy else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("$yr", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "الفصل الدراسي",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1 to "الفصل الأول", 2 to "الفصل الثاني").forEach { (sem, label) ->
                                    val selected = state.semester == sem
                                    Button(
                                        onClick = { onSemesterChange(sem) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selected) DeepNavy else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "المظهر",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { if (state.isDarkMode) onToggleDarkMode() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!state.isDarkMode) DeepNavy else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!state.isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فاتح", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { if (!state.isDarkMode) onToggleDarkMode() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isDarkMode) DeepNavy else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (state.isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("داكن", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("logout_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) DeepNavy else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) DeepNavy else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
