package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val habitType: String, // "build" | "avoid"
    val xpReward: Int,
    val progressive: Boolean,
    val unit: String?,
    val minAmount: Int?,
    val barrierBonus: Int?,
    val frequency: String, // "daily" | "3x" | "2x" | "1x"
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completions: List<String> = emptyList(), // YYYY-MM-DD dates
    val skips: List<String> = emptyList(), // YYYY-MM-DD dates
    val amounts: Map<String, Int> = emptyMap() // YYYY-MM-DD -> amount completed
)
