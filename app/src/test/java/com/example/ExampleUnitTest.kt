package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAdminInitialAddedSubjectsIsZero() {
    val viewModel = com.example.viewmodel.AppViewModel()
    val state = viewModel.uiState.value
    // Initial added subjects for admin must be 0
    assertEquals(0, state.addedSubjectsCount)
    assertEquals(0, state.addedCompletionPercentage)
    assertEquals(60, state.totalSubjectsCount)
  }

  @Test
  fun testTogglePublishSubject() {
    val viewModel = com.example.viewmodel.AppViewModel()
    val testSubjectId = "y1s1-01"

    assertFalse(viewModel.uiState.value.addedSubjectIds.contains(testSubjectId))
    
    // Toggle on (publish)
    viewModel.togglePublishSubject(testSubjectId)
    assertTrue(viewModel.uiState.value.addedSubjectIds.contains(testSubjectId))
    assertEquals(1, viewModel.uiState.value.addedSubjectsCount)

    // Toggle off (unpublish)
    viewModel.togglePublishSubject(testSubjectId)
    assertFalse(viewModel.uiState.value.addedSubjectIds.contains(testSubjectId))
    assertEquals(0, viewModel.uiState.value.addedSubjectsCount)
  }

  @Test
  fun testAddCustomTopicWithFile() {
    val viewModel = com.example.viewmodel.AppViewModel()
    val testSubjectId = "y1s1-01"
    val testAttachment = com.example.model.TopicAttachment(
        id = "file_1",
        name = "Lecture1.pdf",
        sizeFormatted = "1.5 MB",
        supabaseUrl = "https://example.supabase.co/storage/v1/object/public/subject-files/Lecture1.pdf"
    )

    viewModel.addCustomTopic(
        subjectId = testSubjectId,
        title = "المحاضرة 1: مقدمة في البرمجة",
        description = "ملف الشرح الكامل",
        attachments = listOf(testAttachment)
    )

    val topics = viewModel.uiState.value.customTopicsBySubject[testSubjectId]
    assertNotNull(topics)
    assertEquals(1, topics!!.size)
    assertEquals("المحاضرة 1: مقدمة في البرمجة", topics[0].title)
    assertEquals(1, topics[0].attachments.size)
    assertEquals("Lecture1.pdf", topics[0].attachments[0].name)
  }

  @Test
  fun testAdminLogin() {
    val viewModel = com.example.viewmodel.AppViewModel()
    val success = viewModel.login("admin", "123456")
    assertTrue(success)
    assertEquals(com.example.viewmodel.UserRole.ADMIN, viewModel.uiState.value.role)
    assertTrue(viewModel.uiState.value.isLoggedIn)
  }

  @Test
  fun testEmptyCurriculumSectionsAndCleanSubjects() {
    val subjects = com.example.model.CurriculumData.ALL_SUBJECTS
    assertEquals(60, subjects.size)

    // Verify all subjects have only real name, year in 1..5, semester in 1..2
    subjects.forEach { subject ->
      assertTrue(subject.name.isNotBlank())
      assertTrue(subject.year in 1..5)
      assertTrue(subject.semester in 1..2)

      // Verify mock data is completely empty
      assertTrue(com.example.model.CurriculumData.getTopicsForSubject(subject.id).isEmpty())
      assertTrue(com.example.model.CurriculumData.getExamPapersForSubject(subject.id).isEmpty())
      assertTrue(com.example.model.CurriculumData.getUnaskedQuestionsForSubject(subject.id).isEmpty())
    }
  }

  @Test
  fun testInitialStateHasNoMockEnrolledOrCompleted() {
    val viewModel = com.example.viewmodel.AppViewModel()
    val state = viewModel.uiState.value
    assertTrue(state.enrolledSubjectIds.isEmpty())
    assertTrue(state.completedTopicIds.isEmpty())
    assertEquals(0, state.purchasedTotalTopicsCount)
    assertEquals(0, state.purchasedCompletedTopicsCount)
  }
}
