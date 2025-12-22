package com.fredcodecrafts.moodlens.database.repository



import com.fredcodecrafts.moodlens.database.dao.JournalDao
import com.fredcodecrafts.moodlens.ui.screens.MoodLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MoodMapRepository(
    private val journalDao: JournalDao
) {
    /**
     * Retrieves all mood entries with location data for a specific user.
     * Returns a Flow so the UI automatically updates when data changes.
     */
    fun getMoodLocationsForUser(userId: String): Flow<List<MoodLocation>> = flow {
        // Use the optimized DAO method that filters for non-null coordinates
        val entries = journalDao.getEntriesWithLocation(userId)

        val moodLocations = entries
            .filter { entry ->
                // Extra validation: ensure coordinates are valid (not 0.0, 0.0)
                entry.latitude != 0.0 && entry.longitude != 0.0
            }
            .map { entry ->
                MoodLocation(
                    id = entry.entryId.hashCode().toLong(), // Convert String ID to Long
                    mood = entry.mood,
                    latitude = entry.latitude!!,  // Safe because getEntriesWithLocation filters nulls
                    longitude = entry.longitude!!, // Safe because getEntriesWithLocation filters nulls
                    timestamp = entry.timestamp,
                    note = entry.aiReflection // Use aiReflection as the note
                )
            }

        emit(moodLocations)
    }

    /** fun getMoodLocationsForUser(userId: String): Flow<List<MoodLocation>> = flow {
        // Use the optimized DAO method that filters for non-null coordinates
        val entries = journalDao.getEntriesWithLocation(userId)

        val moodLocations = entries
            .filter { entry ->
                // Extra validation: ensure coordinates are valid (not 0.0, 0.0)
                entry.latitude != 0.0 && entry.longitude != 0.0
            }
            .map { entry ->
                MoodLocation(
                    id = entry.entryId.hashCode().toLong(), // Convert String ID to Long
                    mood = entry.mood,
                    latitude = entry.latitude!!,  // Safe because getEntriesWithLocation filters nulls
                    longitude = entry.longitude!!, // Safe because getEntriesWithLocation filters nulls
                    timestamp = entry.timestamp,
                    note = entry.aiReflection // Use aiReflection as the note
                )
            }

        emit(moodLocations)
    }
 */
    /**
     * Retrieves mood locations within a specific time range.
     * Useful for filtering by date (e.g., "Last Month", "This Year").
     *
     * @param userId The user's ID
     * @param startTime Start timestamp in milliseconds
     * @param endTime End timestamp in milliseconds
     */
    suspend fun getMoodLocationsByDateRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): List<MoodLocation> {
        val entries = journalDao.getEntriesWithLocation(userId)

        return entries
            .filter { entry ->
                entry.latitude != 0.0 &&
                        entry.longitude != 0.0 &&
                        entry.timestamp in startTime..endTime
            }
            .map { entry ->
                MoodLocation(
                    id = entry.entryId.hashCode().toLong(),
                    mood = entry.mood,
                    latitude = entry.latitude!!,
                    longitude = entry.longitude!!,
                    timestamp = entry.timestamp,
                    note = entry.aiReflection
                )
            }
    }

    /**
     * Gets the total count of entries with location data.
     * Useful for displaying stats.
     */
    suspend fun getMoodLocationCount(userId: String): Int {
        return journalDao.getEntriesWithLocation(userId).count { entry ->
            entry.latitude != 0.0 && entry.longitude != 0.0
        }
    }

    /**
     * Gets all entries for a user (including those without location).
     * Useful for general journal functionality.
     */
    suspend fun getAllEntriesForUser(userId: String): List<MoodLocation> {
        val entries = journalDao.getEntriesForUser(userId)

        return entries
            .filter { entry ->
                entry.latitude != null &&
                        entry.longitude != null &&
                        entry.latitude != 0.0 &&
                        entry.longitude != 0.0
            }
            .map { entry ->
                MoodLocation(
                    id = entry.entryId.hashCode().toLong(),
                    mood = entry.mood,
                    latitude = entry.latitude!!,
                    longitude = entry.longitude!!,
                    timestamp = entry.timestamp,
                    note = entry.aiReflection
                )
            }
    }
}