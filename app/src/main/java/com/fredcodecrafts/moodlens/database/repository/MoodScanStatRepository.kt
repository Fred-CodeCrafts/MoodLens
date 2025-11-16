package com.fredcodecrafts.moodlens.database.repository

import com.fredcodecrafts.moodlens.database.dao.MoodScanStatDao
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat

class MoodScanStatRepository(
    private val moodScanStatDao: MoodScanStatDao
) {

    suspend fun insert(stat: MoodScanStat) {
        moodScanStatDao.insert(stat)
    }

    suspend fun insertAll(stats: List<MoodScanStat>) {
        moodScanStatDao.insertAll(stats)
    }

    suspend fun getAllStats(): List<MoodScanStat> {
        return moodScanStatDao.getAllStats()
    }

    suspend fun getStatForUserOnDate(userId: String, date: Long): MoodScanStat? {
        return moodScanStatDao.getStatForUserOnDate(userId, date)
    }
}
