package com.fredcodecrafts.moodlens.database.repository


import android.util.Log
import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat
import com.fredcodecrafts.moodlens.utils.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


class MoodScanStatRepository(
    private val moodScanStatDao: MoodScanStatDao
) {
//    private val client = HttpClient(OkHttp) {
//        install(ContentNegotiation) {
//            json(Json {
//                ignoreUnknownKeys = true
//                isLenient = true
//                encodeDefaults = true
//            })
//        }
//    }

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

    // ------------------- FIREBASE REALTIME DB SYNC -------------------
    private val firebaseDb = com.google.firebase.database.FirebaseDatabase.getInstance()
    private val statsRef = firebaseDb.getReference("mood_stats")

    private fun upsertMoodScanStat(stat: MoodScanStat) {
        val userId = SessionManager.currentUserId ?: return
        statsRef.child(userId).child(stat.statId).setValue(stat)
             .addOnSuccessListener {
                 Log.d("MoodScanStatRepository", "Stat synced to Firebase")
             }
             .addOnFailureListener { e ->
                 Log.e("MoodScanStatRepository", "Failed to sync stat", e)
             }
    }

    suspend fun fetchAndSyncMoodStats() {
        val userId = SessionManager.currentUserId ?: return
        
        statsRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val stats = mutableListOf<MoodScanStat>()
                for (child in snapshot.children) {
                    try {
                        val stat = child.getValue(MoodScanStat::class.java)
                        if (stat != null) stats.add(stat)
                    } catch (e: Exception) {
                        Log.e("MoodScanStatRepository", "Error parsing stat: ${e.message}")
                    }
                }
                if (stats.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        moodScanStatDao.insertAll(stats)
                    }
                }
            }
        }.addOnFailureListener { e ->
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

