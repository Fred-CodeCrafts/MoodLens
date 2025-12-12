package com.fredcodecrafts.moodlens.database.repository

import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fredcodecrafts.moodlens.utils.SupabaseClient

class MoodScanStatRepository(
    private val moodScanStatDao: MoodScanStatDao
) {

    suspend fun insert(stat: MoodScanStat) {
        moodScanStatDao.insert(stat)
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseClient.upsertMoodScanStat(stat)
        }
    }

    suspend fun insertAll(stats: List<MoodScanStat>) {
        moodScanStatDao.insertAll(stats)
         CoroutineScope(Dispatchers.IO).launch {
            stats.forEach { SupabaseClient.upsertMoodScanStat(it) }
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
}
