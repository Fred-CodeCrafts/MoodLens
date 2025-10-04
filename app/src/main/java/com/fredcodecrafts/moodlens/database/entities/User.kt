package com.fredcodecrafts.moodlens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_offline")
data class User(
    @PrimaryKey val userId: String,
    val googleId: String
)
