package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao, private val journalDao: JournalDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allJournalEntries: Flow<List<JournalEntity>> = journalDao.getAllEntries()

    suspend fun getHabitById(id: String): HabitEntity? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: HabitEntity) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabitById(id: String) {
        habitDao.deleteHabitById(id)
    }

    suspend fun insertJournalEntry(entry: JournalEntity) {
        journalDao.insertEntry(entry)
    }

    suspend fun deleteJournalEntry(entry: JournalEntity) {
        journalDao.deleteEntry(entry)
    }
}
