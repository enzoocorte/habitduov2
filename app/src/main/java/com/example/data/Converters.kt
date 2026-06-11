package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromMap(map: Map<String, Int>?): String {
        if (map.isNullOrEmpty()) return ""
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toMap(data: String?): Map<String, Int> {
        if (data.isNullOrEmpty()) return emptyMap()
        val map = mutableMapOf<String, Int>()
        data.split(",").forEach { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                val key = parts[0]
                val value = parts[1].toIntOrNull() ?: 0
                map[key] = value
            }
        }
        return map
    }
}
