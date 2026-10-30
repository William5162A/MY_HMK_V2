package com.example.data

import android.util.Log
import com.example.model.AdminTopic
import com.example.model.SupabaseConfig
import com.example.model.TopicAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseDatabaseService {
    private const val TAG = "SupabaseDbService"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun isConfigured(config: SupabaseConfig): Boolean {
        val cleanUrl = config.projectUrl.trim().trimEnd('/')
        return cleanUrl.startsWith("http") &&
                !cleanUrl.contains("your-supabase-project") &&
                config.anonKey.isNotBlank() &&
                !config.anonKey.contains("...")
    }

    // =========================================================================
    // 1. Published Subjects Synchronization (تزامن المواد المنشورة للطلاب)
    // =========================================================================

    suspend fun fetchPublishedSubjects(config: SupabaseConfig): Result<Set<String>> = withContext(Dispatchers.IO) {
        if (!isConfigured(config)) {
            return@withContext Result.failure(Exception("إعدادات Supabase غير مهيأة بعد"))
        }

        try {
            val cleanUrl = config.projectUrl.trim().trimEnd('/')
            val endpoint = "$cleanUrl/rest/v1/published_subjects?is_published=eq.true&select=subject_id"

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", config.anonKey.trim())
                .addHeader("Authorization", "Bearer ${config.anonKey.trim()}")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string().orEmpty()
                val jsonArray = JSONArray(bodyStr)
                val set = mutableSetOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    set.add(obj.optString("subject_id"))
                }
                Result.success(set)
            } else {
                val errorMsg = response.body?.string() ?: "خطأ HTTP ${response.code}"
                Log.w(TAG, "Failed to fetch published subjects: $errorMsg")
                Result.failure(Exception("فشل جلب المواد المنشورة: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchPublishedSubjects", e)
            Result.failure(e)
        }
    }

    suspend fun syncPublishStatus(
        subjectId: String,
        isPublished: Boolean,
        config: SupabaseConfig
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured(config)) {
            // Keep local state only if not configured
            return@withContext Result.success(Unit)
        }

        try {
            val cleanUrl = config.projectUrl.trim().trimEnd('/')
            val endpoint = "$cleanUrl/rest/v1/published_subjects"

            val jsonBody = JSONObject().apply {
                put("subject_id", subjectId)
                put("is_published", isPublished)
                put("updated_at", System.currentTimeMillis())
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", config.anonKey.trim())
                .addHeader("Authorization", "Bearer ${config.anonKey.trim()}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code in 200..299) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body?.string() ?: "خطأ ${response.code}"
                Log.w(TAG, "Failed to update publish status on Supabase: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in syncPublishStatus", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 2. Custom Topics / Lectures Synchronization (تزامن عناوين ومحتويات المواد)
    // =========================================================================

    suspend fun fetchAllTopics(config: SupabaseConfig): Result<Map<String, List<AdminTopic>>> = withContext(Dispatchers.IO) {
        if (!isConfigured(config)) {
            return@withContext Result.failure(Exception("إعدادات Supabase غير مهيأة بعد"))
        }

        try {
            val cleanUrl = config.projectUrl.trim().trimEnd('/')
            val endpoint = "$cleanUrl/rest/v1/topics?select=*&order=created_at.asc"

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", config.anonKey.trim())
                .addHeader("Authorization", "Bearer ${config.anonKey.trim()}")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string().orEmpty()
                val jsonArray = JSONArray(bodyStr)
                val map = mutableMapOf<String, MutableList<AdminTopic>>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val subjectId = obj.optString("subject_id")
                    val title = obj.optString("title")
                    val desc = obj.optString("description")
                    val createdAt = obj.optLong("created_at", System.currentTimeMillis())
                    val attachmentsStr = obj.optString("attachments_json", "[]")

                    val attachments = parseAttachmentsJson(attachmentsStr)

                    val topic = AdminTopic(
                        id = id,
                        subjectId = subjectId,
                        title = title,
                        description = desc,
                        attachments = attachments,
                        createdAt = createdAt
                    )

                    val list = map.getOrPut(subjectId) { mutableListOf() }
                    list.add(topic)
                }

                Result.success(map)
            } else {
                val errorMsg = response.body?.string() ?: "خطأ HTTP ${response.code}"
                Result.failure(Exception("فشل جلب العناوين: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchAllTopics", e)
            Result.failure(e)
        }
    }

    suspend fun insertTopic(
        topic: AdminTopic,
        config: SupabaseConfig
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured(config)) {
            return@withContext Result.success(Unit)
        }

        try {
            val cleanUrl = config.projectUrl.trim().trimEnd('/')
            val endpoint = "$cleanUrl/rest/v1/topics"

            val jsonBody = JSONObject().apply {
                put("id", topic.id)
                put("subject_id", topic.subjectId)
                put("title", topic.title)
                put("description", topic.description)
                put("attachments_json", serializeAttachmentsToJson(topic.attachments))
                put("created_at", topic.createdAt)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", config.anonKey.trim())
                .addHeader("Authorization", "Bearer ${config.anonKey.trim()}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code in 200..299) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body?.string() ?: "خطأ ${response.code}"
                Log.w(TAG, "Failed to insert topic on Supabase: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in insertTopic", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTopic(
        topicId: String,
        config: SupabaseConfig
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured(config)) {
            return@withContext Result.success(Unit)
        }

        try {
            val cleanUrl = config.projectUrl.trim().trimEnd('/')
            val endpoint = "$cleanUrl/rest/v1/topics?id=eq.$topicId"

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", config.anonKey.trim())
                .addHeader("Authorization", "Bearer ${config.anonKey.trim()}")
                .delete()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code in 200..299) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body?.string() ?: "خطأ ${response.code}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in deleteTopic", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // Helpers: JSON Serialization of Attachments
    // =========================================================================

    private fun serializeAttachmentsToJson(attachments: List<TopicAttachment>): String {
        val array = JSONArray()
        for (att in attachments) {
            val obj = JSONObject().apply {
                put("id", att.id)
                put("name", att.name)
                put("sizeFormatted", att.sizeFormatted)
                put("supabaseUrl", att.supabaseUrl)
                put("mimeType", att.mimeType)
                put("uploadDate", att.uploadDate)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseAttachmentsJson(jsonStr: String): List<TopicAttachment> {
        val list = mutableListOf<TopicAttachment>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TopicAttachment(
                        id = obj.optString("id"),
                        name = obj.optString("name", "ملف.pdf"),
                        sizeFormatted = obj.optString("sizeFormatted", "PDF"),
                        supabaseUrl = obj.optString("supabaseUrl"),
                        mimeType = obj.optString("mimeType", "application/pdf"),
                        uploadDate = obj.optString("uploadDate", "")
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }
}
