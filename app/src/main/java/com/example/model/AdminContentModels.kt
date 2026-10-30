package com.example.model

data class TopicAttachment(
    val id: String,
    val name: String,
    val sizeFormatted: String,
    val supabaseUrl: String,
    val localUri: String? = null,
    val mimeType: String = "application/pdf",
    val uploadDate: String = ""
)

data class AdminTopic(
    val id: String,
    val subjectId: String,
    val title: String,
    val description: String = "",
    val attachments: List<TopicAttachment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class SupabaseConfig(
    val projectUrl: String = "https://your-supabase-project.supabase.co",
    val anonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    val bucketName: String = "subject-files"
)
