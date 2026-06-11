package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.Achievement
import com.example.ui.theme.*

@Composable
fun LevelHeader(
    totalXp: Int,
    currentLevel: Int,
    progress: Float,
    dailyXpEarned: Int,
    dailyGoal: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CosmicCardBg, CosmicDeepSpace)
                )
            )
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Level Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CosmicPurple)
                            .testTag("level_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "NIVEL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicSubtle
                            )
                            Text(
                                "$currentLevel",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Habit Duo Space",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicWhite
                        )
                        Text(
                            text = "$totalXp XP Acumulado",
                            fontSize = 12.sp,
                            color = CosmicGold,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right: Mini streak or calendar shortcut
                Box(
                    modifier = Modifier
                        .background(CosmicGray, shape = CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "⚡ ${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = CosmicPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Level Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Lvl $currentLevel",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicSubtle
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = CosmicPurple,
                    trackColor = CosmicGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Lvl ${currentLevel + 1}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicSubtle
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Meta Status Banner
            val progressPercent = if (dailyGoal > 0) (dailyXpEarned.toFloat() / dailyGoal).coerceIn(0f, 1f) else 1f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Meta Diaria (${dailyGoal} XP)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicWhite
                        )
                        Text(
                            "${dailyXpEarned}/${dailyGoal} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dailyXpEarned >= dailyGoal) CosmicGreen else CosmicSubtle
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (dailyXpEarned >= dailyGoal) CosmicGreen else CosmicGold,
                        trackColor = CosmicGray
                    )
                }
                if (dailyXpEarned >= dailyGoal) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("🎉 OK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicGreen)
                }
            }
        }
    }
}

@Composable
fun AchievementDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            color = CosmicCardBg,
            shape = RoundedCornerShape(24.dp),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🏆 ¡NUEVO LOGRO!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicGold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(CosmicPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(achievement.emoji, fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    achievement.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    achievement.description,
                    fontSize = 13.sp,
                    color = CosmicSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "¡Excelente!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
