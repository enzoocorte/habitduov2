package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.JournalEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(viewModel: MainViewModel) {
    val entries by viewModel.journalEntries.collectAsState()

    var textLog by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(2) } // 0 to 4 (defaults to Neutral)

    val moodEmojis = listOf("😢", "😕", "😐", "🙂", "😄")
    val moodLabels = listOf("Terrible", "Mal", "Normal", "Bien", "Genial")

    Scaffold(
        containerColor = CosmicDeepSpace
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Journal logger card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_logger_card"),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "¿Cómo te sientes hoy?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mood selector bubble row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0..4) {
                            val isSelected = selectedMood == i
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedMood = i }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .background(
                                            color = if (isSelected) CosmicPurple else CosmicGray,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(moodEmojis[i], fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = moodLabels[i],
                                    fontSize = 10.sp,
                                    color = if (isSelected) CosmicGold else CosmicSubtle,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text write input
                    Text("Reflexiona sobre tu día...", fontSize = 12.sp, color = CosmicSubtle)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = textLog,
                        onValueChange = { textLog = it },
                        placeholder = { Text("Escribe algo reflexivo (alimenta tu racha de diario y suma XP)...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CosmicDeepSpace,
                            unfocusedContainerColor = CosmicDeepSpace,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = CosmicPurple
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "+15 XP recompensa",
                            fontSize = 11.sp,
                            color = CosmicGold,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                if (textLog.isNotBlank()) {
                                    viewModel.addJournalEntry(selectedMood, textLog.trim())
                                    textLog = ""
                                    selectedMood = 2 // Reset
                                }
                            },
                            enabled = textLog.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CosmicPurple,
                                disabledContainerColor = CosmicGray
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Guardar Entrada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Historias Anteriores",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable list of journal outputs
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Ningún registro diario aún. Registra tus emociones para sumar XP rápida.",
                        fontSize = 13.sp,
                        color = CosmicSubtle,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        JournalItem(
                            entry = entry,
                            moodEmojis = moodEmojis,
                            onDelete = { viewModel.deleteJournalEntry(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JournalItem(
    entry: JournalEntity,
    moodEmojis: List<String>,
    onDelete: () -> Unit
) {
    val datePretty = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.getDefault())
        val dateObj = parser.parse(entry.date)
        if (dateObj != null) formatter.format(dateObj) else entry.date
    } catch (e: Exception) {
        entry.date
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(CosmicDeepSpace, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = moodEmojis.getOrElse(entry.mood) { "😐" },
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = datePretty,
                        fontSize = 11.sp,
                        color = CosmicGold,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = CosmicRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.text,
                    fontSize = 13.sp,
                    color = CosmicWhite
                )
            }
        }
    }
}
