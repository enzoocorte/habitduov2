package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.HabitEntity
import com.example.ui.theme.*

@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    onOpenAddHabit: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val today = viewModel.getLocalDate()

    var activeFilter by remember { mutableStateOf("all") } // "all" | "build" | "avoid" | "archived"

    // Filtered lists
    val filteredHabits = habits.filter { habit ->
        when (activeFilter) {
            "build" -> habit.habitType == "build" && !habit.archived
            "avoid" -> habit.habitType == "avoid" && !habit.archived
            "archived" -> habit.archived
            else -> !habit.archived // "all"
        }
    }

    Scaffold(
        containerColor = CosmicDeepSpace,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddHabit,
                containerColor = CosmicPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Hábito")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs Row
            Row(
                modifier = Modifier.fillMaxWidth().height(42.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "all" to "Todos",
                    "build" to "Construir",
                    "avoid" to "Evitar",
                    "archived" to "Archivados"
                ).forEach { (filterId, label) ->
                    val isSelected = activeFilter == filterId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (isSelected) CosmicPurple else CosmicCardBg,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CosmicPurple else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeFilter = filterId },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else CosmicSubtle,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Habits
            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (activeFilter == "archived") "No tienes hábitos archivados." else "No hay hábitos en esta sección.",
                            fontSize = 14.sp,
                            color = CosmicSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            today = today,
                            streak = viewModel.getHabitStreak(habit),
                            rate = viewModel.getHabitRate(habit),
                            calculatedXp = viewModel.getHabitDayXp(habit, today),
                            onToggle = { viewModel.toggleHabitCompletion(habit.id) },
                            onSkip = { viewModel.toggleHabitSkip(habit.id) },
                            onUpdateAmount = { amount -> viewModel.updateProgressiveAmount(habit.id, amount) },
                            onArchive = { viewModel.archiveHabit(habit.id, !habit.archived) },
                            onDelete = { viewModel.deleteHabit(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    today: String,
    streak: Int,
    rate: Float,
    calculatedXp: Int,
    onToggle: () -> Unit,
    onSkip: () -> Unit,
    onUpdateAmount: (Int) -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = habit.completions.contains(today)
    val isSkipped = habit.skips.contains(today)
    val amount = habit.amounts[today] ?: 0
    val isBuild = habit.habitType == "build"
    val isProgressive = habit.progressive
    val minAmount = habit.minAmount ?: 1
    val barrierBonus = habit.barrierBonus ?: (minAmount * 2)
    val unit = habit.unit ?: "unidades"
    val reachedMin = isProgressive && amount >= minAmount
    val isDaily = habit.frequency == "daily"

    // Custom background based on status matching HabitCard.tsx
    val bgColor = when {
        isBuild && isProgressive && reachedMin -> Color(0xFF1A3A2A) // Rich success green
        isBuild && !isProgressive && isCompleted -> Color(0xFF1A3A2A) // Successful build
        !isBuild && !isCompleted && !isSkipped -> Color(0xFF1A3A2A)  // Successful avoid
        !isBuild && isCompleted -> Color(0xFF3A1A1A)                // Fallen avoid (Failure Red)
        isSkipped -> Color(0xFF2A2A3A)                             // Gray skipped style
        else -> CosmicCardBg                                       // Default neutral slate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First row: Emoji, Name, info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            habit.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBuild) "Construir" else "Evitar",
                                fontSize = 11.sp,
                                color = if (isBuild) CosmicGreen else CosmicRed
                            )
                            Text(" · ", fontSize = 11.sp, color = CosmicSubtle)
                            Text("${habit.xpReward} XP", fontSize = 11.sp, color = CosmicGold)
                            Text(" · ", fontSize = 11.sp, color = CosmicSubtle)
                            Text(
                                text = when (habit.frequency) {
                                    "3x" -> "3x/sem"
                                    "2x" -> "2x/sem"
                                    "1x" -> "1x/sem"
                                    else -> "Diario"
                                },
                                fontSize = 11.sp,
                                color = if (isDaily) CosmicSubtle else CosmicGold
                            )
                        }
                    }
                }

                // Streak & Today's XP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streak > 1) {
                        Text(
                            "🔥$streak ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicGold
                        )
                    }
                    Text(
                        text = if (calculatedXp >= 0) "+$calculatedXp XP" else "$calculatedXp XP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (calculatedXp > 0) CosmicGreen else if (calculatedXp < 0) CosmicRed else CosmicSubtle
                    )
                }
            }

            if (!isDaily) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aviso: No cuenta para la meta diaria automática.",
                    fontSize = 9.sp,
                    color = CosmicGold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7-day rate progress indicator
            val rateProgress = rate.coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Efectividad semanal: ${(rateProgress * 100).toInt()}%", fontSize = 10.sp, color = CosmicSubtle)
                LinearProgressIndicator(
                    progress = { rateProgress },
                    modifier = Modifier
                        .width(80.dp)
                        .height(5.dp)
                        .clip(CircleShape),
                    color = if (rateProgress > 0.7f) CosmicGreen else if (rateProgress > 0.4f) CosmicGold else CosmicRed,
                    trackColor = CosmicGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Layout depends on habit type and progress state
            if (isBuild && isProgressive) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "⚡ Bonus barrera: $minAmount $unit = $barrierBonus XP, luego +1 XP por $unit extra.",
                        fontSize = 10.sp,
                        color = CosmicSubtle,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$amount",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (reachedMin) CosmicGreen else CosmicRed
                            )
                            Text(" / $minAmount $unit mínimo", fontSize = 12.sp, color = CosmicSubtle, modifier = Modifier.padding(start = 4.dp))
                            if (reachedMin) {
                                Text(" ✅", fontSize = 12.sp)
                            }
                        }
                        if (!reachedMin && amount > 0) {
                            Text("Faltan ${minAmount - amount} $unit", fontSize = 11.sp, color = CosmicGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { qa ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (reachedMin) CosmicGreen else CosmicPurple,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onUpdateAmount(amount + qa) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+$qa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Custom number manual entry + Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var amountText by remember(amount) { mutableStateOf(amount.toString()) }
                        TextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                val num = it.toIntOrNull() ?: 0
                                if (num >= 0) onUpdateAmount(num)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.Black),
                            placeholder = { Text("Monto manual", fontSize = 10.sp, color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (amount > 0) {
                            Button(
                                onClick = { onUpdateAmount(0) },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else if (isBuild && !isProgressive) {
                // Button for normal build completion
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) CosmicGreen else CosmicPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isCompleted) "✓ Completado" else "Completar (+${habit.xpReward} XP)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Avoid habit controls (Caiste vs Evitado)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onToggle,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) CosmicRed else CosmicGray
                        ),
                        border = if (!isCompleted) BorderStroke(1.dp, CosmicRed) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "Caíste (-${habit.xpReward} XP)" else "Evitado (+${habit.xpReward} XP)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color.White else CosmicRed
                        )
                    }
                    if (!isCompleted) {
                        Text(
                            "¡Vas genial! No has caído hoy.",
                            fontSize = 10.sp,
                            color = CosmicGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer controls: Skip, Archive, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.05f))
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Only standard uncompleted builds can skip
                    if (!isSkipped && !isCompleted && isBuild && !isProgressive) {
                        Box(
                            modifier = Modifier
                                .background(CosmicGray, RoundedCornerShape(6.dp))
                                .clickable { onSkip() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Saltar", fontSize = 10.sp, color = CosmicSubtle, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!isSkipped && !reachedMin && isBuild && isProgressive) {
                        Box(
                            modifier = Modifier
                                .background(CosmicGray, RoundedCornerShape(6.dp))
                                .clickable { onSkip() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Saltar", fontSize = 10.sp, color = CosmicSubtle, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (isSkipped) {
                        Box(
                            modifier = Modifier
                                .background(CosmicPurple, RoundedCornerShape(6.dp))
                                .clickable { onSkip() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("No saltar", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(CosmicGray, RoundedCornerShape(6.dp))
                            .clickable { onArchive() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (habit.archived) "Desarchivarse" else "Archivar",
                            fontSize = 10.sp,
                            color = CosmicSubtle,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(CosmicGray, RoundedCornerShape(6.dp))
                            .clickable { onDelete() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Eliminar", fontSize = 10.sp, color = CosmicRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
