package com.fredcodecrafts.moodlens.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3, // ⬅️ incremented because we added a new column
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

        // 🆕 Migration object: add 'location' column to 'journal_entries'
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN location TEXT")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tambahkan kolom baru aiReflection
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN aiReflection TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emotion_journal_db"
                )
                    // 🔧 Register the migration here
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
