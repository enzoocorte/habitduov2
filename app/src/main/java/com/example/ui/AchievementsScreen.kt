package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(viewModel: MainViewModel) {
    val unlockedSet by viewModel.unlockedAchievements.collectAsState()
    val allAchievements = viewModel.achievementsList

    val totalAchievements = allAchievements.size
    val unlockedCount = unlockedSet.size
    val unlockRatio = if (totalAchievements > 0) unlockedCount.toFloat() / totalAchievements else 0f

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

            // Unlocks Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Progreso de Logros",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicWhite
                            )
                            Text(
                                "$unlockedCount de $totalAchievements completados",
                                fontSize = 12.sp,
                                color = CosmicGold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(CosmicPurple, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${(unlockRatio * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { unlockRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = CosmicGold,
                        trackColor = CosmicGray,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Galería de Medallas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of achievement medallions
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allAchievements) { ach ->
                    val isUnlocked = unlockedSet.contains(ach.id)
                    AchievementCard(
                        emoji = ach.emoji,
                        name = ach.name,
                        description = ach.description,
                        isUnlocked = isUnlocked
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    emoji: String,
    name: String,
    description: String,
    isUnlocked: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .background(
                color = if (isUnlocked) CosmicCardBg else CosmicCardBg.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (isUnlocked) CosmicGold else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        color = if (isUnlocked) CosmicPurple.copy(alpha = 0.2f) else CosmicGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnlocked) emoji else "🔒",
                    fontSize = 20.sp,
                    modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) CosmicWhite else CosmicSubtle,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 9.sp,
                color = CosmicSubtle,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            
            if (isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Desbloqueado",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicGreen
                )
            }
        }
    }
}
