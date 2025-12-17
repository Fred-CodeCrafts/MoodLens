package com.fredcodecrafts.moodlens.database.repository


import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.utils.SessionManager
import com.fredcodecrafts.moodlens.utils.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MoodScanStatRepository(
    private val moodScanStatDao: MoodScanStatDao
) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    suspend fun insert(stat: MoodScanStat) {
        Log.d("MoodScanStatRepository", "Inserting local stat: ${stat.statId}")
        moodScanStatDao.insert(stat)
        
        try {
            upsertMoodScanStat(stat)
        } catch (e: Exception) {
            Log.e("MoodScanStatRepository", "Remote sync failed, local saved", e)
        }
    }

    suspend fun insertAll(stats: List<MoodScanStat>) {
        moodScanStatDao.insertAll(stats)
        try {
            stats.forEach { upsertMoodScanStat(it) }
        } catch (e: Exception) {
             Log.e("MoodScanStatRepository", "Remote batch sync failed", e)
        }
    }

    suspend fun getAllStats(): List<MoodScanStat> {
        return moodScanStatDao.getAllStats()
    }

    suspend fun getStatForUserOnDate(userId: String, date: Long): MoodScanStat? {
        return moodScanStatDao.getStatForUserOnDate(userId, date)
    }

    suspend fun getStatsForUser(userId: String): List<MoodScanStat> {
        return moodScanStatDao.getStatsForUser(userId)
    }

    // ------------------- SUPABASE LOGIC -------------------

    private suspend fun upsertMoodScanStat(stat: MoodScanStat) {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.post("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.mood_scan_stats") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                header("Accept-Profile", SupabaseConfig.SCHEMA)
                header("Content-Profile", SupabaseConfig.SCHEMA)
                contentType(ContentType.Application.Json)
                setBody(
                    RemoteMoodScanStat(
                        stat_id = stat.statId,
                        user_id = stat.userId,
                        date = stat.date,
                        daily_scans = stat.dailyScans,
                        week_streak = stat.weekStreak,
                        can_access_insights = stat.canAccessInsights
                    )
                )
            }
            if (response.status.value !in 200..299) {
                 Log.e("MoodScanStatRepository", "Failed to sync stat: ${response.bodyAsText()}")
            } else {
                 Log.d("MoodScanStatRepository", "Stat synced.")
            }
        } catch (e: Exception) {
             Log.e("MoodScanStatRepository", "Error syncing stat", e)
        }
    }

    suspend fun fetchAndSyncMoodStats() {
        val token = SessionManager.accessToken ?: return
        try {
            val response = client.get("${SupabaseConfig.SUPABASE_URL}/rest/v1/${SupabaseConfig.SCHEMA}.mood_scan_stats?select=*") {
                header("Authorization", "Bearer $token")
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Accept-Profile", SupabaseConfig.SCHEMA)
            }
            val remoteList: List<RemoteMoodScanStat> = response.body()
             val stats = remoteList.map {
                MoodScanStat(
                    statId = it.stat_id,
                    userId = it.user_id,
                    date = it.date,
                    dailyScans = it.daily_scans,
                    weekStreak = it.week_streak,
                    canAccessInsights = it.can_access_insights
                )
            }
            if (stats.isNotEmpty()) {
                moodScanStatDao.insertAll(stats)
            }
        } catch (e: Exception) {
            Log.e("MoodScanStatRepository", "Error fetching stats", e)
        }
    }

    suspend fun pushAllStats() {
        val userId = SessionManager.currentUserId ?: return
        val allStats = moodScanStatDao.getStatsForUser(userId)
        allStats.forEach { upsertMoodScanStat(it) }
    }




    @Serializable
    private data class RemoteMoodScanStat(
        val stat_id: String,
        val user_id: String,
        val date: Long,
        val daily_scans: Int,
        val week_streak: Int,
        val can_access_insights: Boolean
    )
}

