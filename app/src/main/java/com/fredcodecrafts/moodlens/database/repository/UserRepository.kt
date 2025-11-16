package com.fredcodecrafts.moodlens.database.repository

import com.fredcodecrafts.moodlens.database.dao.UserDao
import com.fredcodecrafts.moodlens.database.entities.User

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    suspend fun insertUsers(users: List<User>) {
        userDao.insertAll(users)
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
}
