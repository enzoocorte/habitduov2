package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val autoGoal by viewModel.autoGoal.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationInterval by viewModel.notificationInterval.collectAsState()
    val smartNotifications by viewModel.smartNotifications.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

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

            // Section: Goals
            Text("METAS DE NIVEL", fontSize = 11.sp, color = CosmicGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Meta Diaria Automática", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicWhite)
                            Text("Calculada según tus hábitos activos diarios", fontSize = 10.sp, color = CosmicSubtle)
                        }
                        Switch(
                            checked = autoGoal,
                            onCheckedChange = { viewModel.setAutoGoal(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CosmicPurple)
                        )
                    }

                    if (!autoGoal) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Ajustar Meta Diaria Manualmente: $dailyGoal XP", fontSize = 12.sp, color = CosmicSubtle)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(20, 50, 75, 100).forEach { goalValue ->
                                val isSelected = dailyGoal == goalValue
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) CosmicPurple else CosmicGray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setDailyGoal(goalValue) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$goalValue XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section: Notifications (Crucial!)
            Text("NOTIFICACIONES Y RECORDATORIOS", fontSize = 11.sp, color = CosmicGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recordatorios de Disciplina", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicWhite)
                            Text("Mantener tus rachas con alarmas locales", fontSize = 10.sp, color = CosmicSubtle)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CosmicPurple)
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Frecuencia del Recordatorio: Cada $notificationInterval horas", fontSize = 12.sp, color = CosmicSubtle)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(2, 4, 8, 12, 24).forEach { hours ->
                                val isSelected = notificationInterval == hours
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) CosmicPurple else CosmicGray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setNotificationInterval(hours) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${hours}h", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mensajes Inteligentes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicWhite)
                                Text("Personalizados sobre tus hábitos restantes", fontSize = 10.sp, color = CosmicSubtle)
                            }
                            Switch(
                                checked = smartNotifications,
                                onCheckedChange = { viewModel.setSmartNotifications(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CosmicPurple)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Test Notification Button
                        Button(
                            onClick = { viewModel.triggerTestNotification() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_notif_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicGray),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⚡ Probar Recordatorio Ahora", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicGold)
                        }
                        Text(
                            text = "Fuerza un recordatorio push local de inmediato para testear la redacción inteligente.",
                            fontSize = 9.sp,
                            color = CosmicSubtle,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section: Security / Danger
            Text("SEGURIDAD Y DATOS", fontSize = 11.sp, color = CosmicGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Restablecer Habit Duo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text(
                        text = "Elimina permanentemente todo tu historial, XP, hábitos y logros para volver a empezar desde cero.",
                        fontSize = 9.sp,
                        color = CosmicSubtle,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                    )
                }
            }

            // Confirmation dialogue
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("¿Estás completamente seguro?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    text = { Text("Esta acción es irreversible y borrará todo tu progreso.", fontSize = 13.sp, color = CosmicSubtle) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetAllData()
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicRed)
                        ) {
                            Text("Restablecer", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancelar", color = CosmicSubtle)
                        }
                    },
                    containerColor = CosmicCardBg
                )
            }
        }
    }
}
