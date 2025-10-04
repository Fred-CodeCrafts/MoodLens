package com.fredcodecrafts.moodlens.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.MoodScanStat

@Dao
interface MoodScanStatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: MoodScanStat)

    @Query("SELECT * FROM mood_scan_stats_offline WHERE userId = :userId AND date = :date")
    suspend fun getStatForUserOnDate(userId: String, date: Long): MoodScanStat?
}
