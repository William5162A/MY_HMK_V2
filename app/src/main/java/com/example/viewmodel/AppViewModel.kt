package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SupabaseDatabaseService
import com.example.model.CurriculumData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserRole {
    ADMIN,
    STUDENT
}

data class YearAddedStat(
    val year: Int,
    val count: Int
)

data class AppUiState(
    val isLoggedIn: Boolean = false,
    val username: String = "admin",
    val displayName: String = "المشرف العام",
    val role: UserRole = UserRole.ADMIN,
    val isDarkMode: Boolean = true,
    val currentTab: Int = 0, // 0: الرئيسية, 1: موادي, 2: تصفح المواد, 3: ملفي الشخصي
    val academicYear: Int = 1,
    val semester: Int = 1,
    val enrolledSubjectIds: Set<String> = emptySet(), // Starts empty, populated when subjects are enrolled/published
    val completedTopicIds: Set<String> = emptySet(), // Starts empty, no fake mock completions
    val selectedSubjectId: String? = null, // Opened subject for detail view
    val searchQuery: String = "",
    val filterYear: Int? = null,
    val filterSemester: Int? = null,
    val loginError: String? = null,
    // Added subjects published for students by admin - starts at 0 initially as requested
    val addedSubjectIds: Set<String> = emptySet(),
    // Custom topics and uploaded files created by Admin per subject
    val customTopicsBySubject: Map<String, List<com.example.model.AdminTopic>> = emptyMap(),
    // Supabase storage & database credentials/config
    val supabaseConfig: com.example.model.SupabaseConfig = com.example.model.SupabaseConfig(),
    val isSyncingWithSupabase: Boolean = false,
    val supabaseSyncStatusMessage: String? = null
) {
    // Total topics count belonging ONLY to purchased/enrolled subjects (from real topics added by admin)
    val purchasedTotalTopicsCount: Int
        get() = enrolledSubjectIds.sumOf { (customTopicsBySubject[it]?.size ?: 0) }

    // Completed topics count belonging ONLY to purchased/enrolled subjects
    val purchasedCompletedTopicsCount: Int
        get() = enrolledSubjectIds.sumOf { subId ->
            val subTopics = customTopicsBySubject[subId].orEmpty()
            subTopics.count { completedTopicIds.contains(it.id) }
        }

    // Progress percentage of purchased subjects
    val purchasedProgressPercentage: Int
        get() = if (purchasedTotalTopicsCount > 0) {
            (purchasedCompletedTopicsCount * 100) / purchasedTotalTopicsCount
        } else 0

    // Admin Stats:
    val totalSubjectsCount: Int
        get() = CurriculumData.TOTAL_SUBJECTS_COUNT // 60

    val addedSubjectsCount: Int
        get() = addedSubjectIds.size

    val addedCompletionPercentage: Int
        get() = if (totalSubjectsCount > 0) {
            (addedSubjectsCount * 100) / totalSubjectsCount
        } else 0

    // Academic Year with the lowest number of added subjects
    val yearWithLowestAddedSubjects: YearAddedStat
        get() {
            val stats = (1..5).map { yr ->
                val count = addedSubjectIds.count { subId ->
                    CurriculumData.getSubjectById(subId)?.year == yr
                }
                YearAddedStat(yr, count)
            }
            return stats.minByOrNull { it.count } ?: YearAddedStat(5, 0)
        }
}

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        syncWithSupabase()
    }

    fun syncWithSupabase() {
        val config = _uiState.value.supabaseConfig
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingWithSupabase = true) }

            // 1. مزامنة المواد المنشورة من Supabase
            val publishedRes = SupabaseDatabaseService.fetchPublishedSubjects(config)
            if (publishedRes.isSuccess) {
                val remotePublished = publishedRes.getOrNull().orEmpty()
                _uiState.update { state ->
                    val merged = state.addedSubjectIds + remotePublished
                    state.copy(addedSubjectIds = merged)
                }
            }

            // 2. مزامنة الدروس والمحاضرات من Supabase
            val topicsRes = SupabaseDatabaseService.fetchAllTopics(config)
            if (topicsRes.isSuccess) {
                val remoteTopics = topicsRes.getOrNull().orEmpty()
                _uiState.update { state ->
                    val updatedMap = state.customTopicsBySubject.toMutableMap()
                    remoteTopics.forEach { (sId, list) ->
                        val localList = updatedMap[sId].orEmpty()
                        val combined = (localList + list).distinctBy { it.id }
                        updatedMap[sId] = combined
                    }
                    state.copy(customTopicsBySubject = updatedMap)
                }
            }

            val syncMsg = if (publishedRes.isSuccess || topicsRes.isSuccess) {
                "تمت المزامنة بنجاح مع Supabase"
            } else null

            _uiState.update {
                it.copy(
                    isSyncingWithSupabase = false,
                    supabaseSyncStatusMessage = syncMsg
                )
            }
        }
    }

    fun login(identifier: String, pass: String): Boolean {
        val cleanUser = identifier.trim()
        val cleanPass = pass.trim()

        if (cleanUser.equals("admin", ignoreCase = true) && cleanPass == "123456") {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    username = "admin",
                    displayName = "مسؤول النظام (Admin)",
                    role = UserRole.ADMIN,
                    loginError = null,
                    currentTab = 0,
                    selectedSubjectId = null
                )
            }
            return true
        }

        if (cleanUser.equals("user", ignoreCase = true) && cleanPass == "123456") {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    username = "user",
                    displayName = "طالب جامعي (Student)",
                    role = UserRole.STUDENT,
                    loginError = null,
                    currentTab = 0,
                    selectedSubjectId = null
                )
            }
            return true
        }

        _uiState.update {
            it.copy(loginError = "بيانات الدخول غير صحيحة. يرجى استخدام admin/123456 أو user/123456")
        }
        return false
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                username = "",
                displayName = "",
                currentTab = 0,
                selectedSubjectId = null,
                loginError = null
            )
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setDarkMode(dark: Boolean) {
        _uiState.update { it.copy(isDarkMode = dark) }
    }

    fun setCurrentTab(tab: Int) {
        _uiState.update { it.copy(currentTab = tab, selectedSubjectId = null) }
    }

    fun selectSubject(subjectId: String?) {
        _uiState.update { it.copy(selectedSubjectId = subjectId) }
    }

    fun toggleTopicCompletion(topicId: String) {
        _uiState.update { state ->
            val updated = state.completedTopicIds.toMutableSet()
            if (updated.contains(topicId)) {
                updated.remove(topicId)
            } else {
                updated.add(topicId)
            }
            state.copy(completedTopicIds = updated)
        }
    }

    fun setAcademicYear(year: Int) {
        _uiState.update { it.copy(academicYear = year) }
    }

    fun setSemester(sem: Int) {
        _uiState.update { it.copy(semester = sem) }
    }

    fun toggleEnroll(subjectId: String) {
        _uiState.update { state ->
            val updated = state.enrolledSubjectIds.toMutableSet()
            if (updated.contains(subjectId)) {
                updated.remove(subjectId)
            } else {
                updated.add(subjectId)
            }
            state.copy(enrolledSubjectIds = updated)
        }
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun setFilterYear(year: Int?) {
        _uiState.update { it.copy(filterYear = year) }
    }

    fun setFilterSemester(sem: Int?) {
        _uiState.update { it.copy(filterSemester = sem) }
    }

    fun togglePublishSubject(subjectId: String) {
        var willBePublished = false
        _uiState.update { state ->
            val updated = state.addedSubjectIds.toMutableSet()
            if (updated.contains(subjectId)) {
                updated.remove(subjectId)
                willBePublished = false
            } else {
                updated.add(subjectId)
                willBePublished = true
            }
            state.copy(addedSubjectIds = updated)
        }

        // مزامنة حالة النشر فورياً مع قاعدة بيانات Supabase
        viewModelScope.launch {
            val config = _uiState.value.supabaseConfig
            SupabaseDatabaseService.syncPublishStatus(subjectId, willBePublished, config)
        }
    }

    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun addCustomTopic(
        subjectId: String,
        title: String,
        description: String = "",
        attachments: List<com.example.model.TopicAttachment>
    ) {
        val newTopic = com.example.model.AdminTopic(
            id = "${subjectId}_topic_${System.currentTimeMillis()}",
            subjectId = subjectId,
            title = title.trim(),
            description = description.trim(),
            attachments = attachments
        )

        _uiState.update { state ->
            val existing = state.customTopicsBySubject[subjectId].orEmpty()
            val updatedMap = state.customTopicsBySubject.toMutableMap()
            updatedMap[subjectId] = existing + newTopic
            state.copy(customTopicsBySubject = updatedMap)
        }

        // حفظ العنوان مع مرفقاته السحابية في جدول topics على Supabase
        viewModelScope.launch {
            val config = _uiState.value.supabaseConfig
            SupabaseDatabaseService.insertTopic(newTopic, config)
        }
    }

    fun removeCustomTopic(subjectId: String, topicId: String) {
        _uiState.update { state ->
            val existing = state.customTopicsBySubject[subjectId].orEmpty()
            val filtered = existing.filterNot { it.id == topicId }
            val updatedMap = state.customTopicsBySubject.toMutableMap()
            updatedMap[subjectId] = filtered
            state.copy(customTopicsBySubject = updatedMap)
        }

        // حذف العنوان من جدول topics على Supabase
        viewModelScope.launch {
            val config = _uiState.value.supabaseConfig
            SupabaseDatabaseService.deleteTopic(topicId, config)
        }
    }

    fun updateSupabaseConfig(config: com.example.model.SupabaseConfig) {
        _uiState.update { it.copy(supabaseConfig = config) }
        syncWithSupabase()
    }
}
