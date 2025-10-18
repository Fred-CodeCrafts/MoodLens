package com.fredcodecrafts.moodlens.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fredcodecrafts.moodlens.database.dao.*
import com.fredcodecrafts.moodlens.database.entities.*

@Database(
    entities = [
        User::class,
        JournalEntry::class,
        Note::class,
        Message::class,
        Question::class,
        MoodScanStat::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun notesDao(): NotesDao
    abstract fun messagesDao(): MessagesDao
    abstract fun questionsDao(): QuestionsDao
    abstract fun moodScanStatDao(): MoodScanStatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emotion_journal_db"
                )
                    // Wipe and recreate DB automatically if schema changed
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
