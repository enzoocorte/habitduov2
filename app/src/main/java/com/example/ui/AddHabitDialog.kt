package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (
        name: String,
        emoji: String,
        habitType: String,
        xpReward: Int,
        progressive: Boolean,
        unit: String?,
        minAmount: Int?,
        barrierBonus: Int?,
        frequency: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("✨") }
    var habitType by remember { mutableStateOf("build") } // "build" | "avoid"
    var frequency by remember { mutableStateOf("daily") } // "daily", "3x", "2x", "1x"
    var progressive by remember { mutableStateOf(false) }
    var xpReward by remember { mutableStateOf("20") }
    var unit by remember { mutableStateOf("min") }
    var minAmount by remember { mutableStateOf("5") }
    var barrierBonus by remember { mutableStateOf("10") }

    val emojiOptions = listOf("🏃", "📖", "🧘", "💪", "🎸", "✍️", "🥗", "💧", "🛡️", "📱", "🍔", "🚬", "😴", "✨", "🎯", "⭐")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            color = CosmicDeepSpace,
            shape = RoundedCornerShape(24.dp),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Hábito",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = CosmicSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Input
                Text("Nombre", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicSubtle)
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ej: Leer 20 minutos", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CosmicCardBg,
                        unfocusedContainerColor = CosmicCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CosmicPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emoji Picker
                Text("Emoji", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicSubtle)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 8,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojiOptions.forEach { em ->
                        val isSelected = selectedEmoji == em
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isSelected) CosmicPurple else CosmicCardBg,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedEmoji = em },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = em, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type
                Text("Tipo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicSubtle)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { habitType = "build" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (habitType == "build") CosmicGreen else CosmicCardBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Construir", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            habitType = "avoid" 
                            progressive = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (habitType == "avoid") CosmicRed else CosmicCardBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Evitar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency
                Text("Frecuencia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicSubtle)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("daily" to "Diario", "3x" to "3x/sem", "2x" to "2x/sem", "1x" to "1x/sem").forEach { (freqId, label) ->
                        val isSelected = frequency == freqId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(
                                    color = if (isSelected) CosmicPurple else CosmicCardBg,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { frequency = freqId },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CosmicSubtle
                            )
                        }
                    }
                }
                Text(
                    text = "Solo hábitos de frecuencia 'Diario' computan para el cálculo de la meta diaria.",
                    fontSize = 10.sp,
                    color = CosmicSubtle,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progressive Toggle for Build Habits
                if (habitType == "build") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Progresivo (cantidad variable)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Ideal para medir minutos, kilómetros, páginas...", fontSize = 10.sp, color = CosmicSubtle)
                        }
                        Switch(
                            checked = progressive,
                            onCheckedChange = { progressive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CosmicGreen
                            )
                        )
                    }

                    if (progressive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicCardBg, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            // Unit
                            Text("Unidad", fontSize = 11.sp, color = CosmicSubtle)
                            TextField(
                                value = unit,
                                onValueChange = { unit = it },
                                placeholder = { Text("Ej: km, min, páginas") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = CosmicDeepSpace,
                                    unfocusedContainerColor = CosmicDeepSpace,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Barrier (Min Amount)
                            Text("Cantidad mínima (barrera)", fontSize = 11.sp, color = CosmicSubtle)
                            TextField(
                                value = minAmount,
                                onValueChange = { minAmount = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = CosmicDeepSpace,
                                    unfocusedContainerColor = CosmicDeepSpace,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Text("La barrera es lo más difícil: arrancar. Por eso se premia más.", fontSize = 9.sp, color = CosmicGold)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Barrier Bonus (XP)
                            Text("Bonus barrera (XP al romperla)", fontSize = 11.sp, color = CosmicSubtle)
                            TextField(
                                value = barrierBonus,
                                onValueChange = { barrierBonus = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = CosmicDeepSpace,
                                    unfocusedContainerColor = CosmicDeepSpace,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Text("Al llegar al mínimo → +$barrierBonus XP. Luego +1 XP por cada unidad extra.", fontSize = 9.sp, color = CosmicSubtle)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // XP Reward Input
                Text(
                    text = if (habitType == "avoid") "Pérdida XP (si caes)" else if (progressive) "Límite XP (máximo diario)" else "Recompensa XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicSubtle
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = xpReward,
                    onValueChange = { xpReward = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CosmicCardBg,
                        unfocusedContainerColor = CosmicCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CosmicPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Create Action
                Button(
                    onClick = {
                        val finalMin = minAmount.toIntOrNull() ?: 5
                        val finalBonus = barrierBonus.toIntOrNull() ?: 10
                        val finalReward = xpReward.toIntOrNull() ?: 20
                        if (name.isNotBlank()) {
                            onAdd(
                                name.trim(),
                                selectedEmoji,
                                habitType,
                                finalReward,
                                progressive,
                                if (progressive) unit else null,
                                if (progressive) finalMin else null,
                                if (progressive) finalBonus else null,
                                frequency
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPurple,
                        disabledContainerColor = CosmicCardBg
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Crear Hábito",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (name.isNotBlank()) Color.White else Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
