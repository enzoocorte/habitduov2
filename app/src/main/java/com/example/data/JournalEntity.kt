package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val mood: Int, // 0 to 4 (e.g. 😢, 😕, 😐, 🙂, 😄)
    val text: String,
    val xp: Int,
    val timestamp: Long = System.currentTimeMillis()
)
