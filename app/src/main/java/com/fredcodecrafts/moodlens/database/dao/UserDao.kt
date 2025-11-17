package com.fredcodecrafts.moodlens.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fredcodecrafts.moodlens.database.entities.User

@Dao
interface UserDao {

    private fun simpleXOR(input: String, key: Char = 'K'): String {
        return input.map { it.code.xor(key.code).toChar() }.joinToString("")
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRaw(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaw(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsersRaw(): List<User>

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUserByIdRaw(id: String): User?

    @Query("SELECT * FROM users WHERE googleId = :googleId")
    suspend fun getUserByGoogleIdRaw(googleId: String): User?

    // --- Helper functions with XOR obfuscation ---
    suspend fun insertAll(users: List<User>) {
        val obfuscated = users.map {
            it.copy(googleId = simpleXOR(it.googleId))
        }
        insertAllRaw(obfuscated)
    }

    suspend fun insert(user: User) {
        val obfuscated = user.copy(googleId = simpleXOR(user.googleId))
        insertRaw(obfuscated)
    }

    suspend fun getAllUsers(): List<User> {
        return getAllUsersRaw().map {
            it.copy(googleId = simpleXOR(it.googleId))
        }
    }

    suspend fun getUserById(id: String): User? {
        return getUserByIdRaw(id)?.let {
            it.copy(googleId = simpleXOR(it.googleId))
        }
    }

    suspend fun getUserByGoogleId(googleId: String): User? {
        return getUserByGoogleIdRaw(simpleXOR(googleId))?.let {
            it.copy(googleId = simpleXOR(it.googleId))
        }
    }
}
