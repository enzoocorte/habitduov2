package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Sync states if granted
        if (isGranted) {
            val mainViewModel = MainViewModel(application)
            mainViewModel.setNotificationsEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and prompt POST_NOTIFICATIONS runtime permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                
                // Observe data models reactively
                val habits by viewModel.habits.collectAsState()
                val journals by viewModel.journalEntries.collectAsState()
                
                val totalXp by viewModel.totalXp.collectAsState()
                val level by viewModel.currentLevel.collectAsState()
                val progress by viewModel.nextLevelProgress.collectAsState()
                
                val autoGoal by viewModel.autoGoal.collectAsState()
                val dailyGoal by viewModel.dailyGoal.collectAsState()
                val newlyUnlockedAchievement by viewModel.newlyUnlockedAchievement.collectAsState()

                // Calculate Automatic Daily Goal & Today's XP Completion
                val todayStr = viewModel.getLocalDate()
                
                // Auto goal: combined sumOf daily habits rewards (excluding archived ones)
                val calculatedAutoGoal = remember(habits) {
                    val dailyActiveBuilds = habits.filter { 
                        it.habitType == "build" && it.frequency == "daily" && !it.archived 
                    }
                    dailyActiveBuilds.sumOf { it.xpReward }.coerceAtLeast(30)
                }

                // Push auto goal calculation when enabled
                LaunchedEffect(autoGoal, calculatedAutoGoal) {
                    if (autoGoal) {
                        viewModel.setDailyGoal(calculatedAutoGoal)
                    }
                }

                val dailyXpEarned = remember(habits, journals, todayStr) {
                    val habitXp = habits.filter { !it.archived }.sumOf { viewModel.getHabitDayXp(it, todayStr) }
                    val journalXp = journals.filter { it.date == todayStr }.sumOf { it.xp }
                    (habitXp + journalXp).coerceAtLeast(0)
                }

                var currentTab by remember { mutableStateOf("habits") }
                var showAddHabitDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CosmicDeepSpace,
                    topBar = {
                        LevelHeader(
                            totalXp = totalXp,
                            currentLevel = level,
                            progress = progress,
                            dailyXpEarned = dailyXpEarned,
                            dailyGoal = dailyGoal
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CosmicCardBg,
                            contentColor = CosmicSubtle,
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "habits",
                                onClick = { currentTab = "habits" },
                                icon = { Icon(Icons.Default.List, contentDescription = "Hábitos") },
                                label = { Text("Hábitos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = CosmicSubtle,
                                    unselectedTextColor = CosmicSubtle,
                                    indicatorColor = CosmicPurple
                                ),
                                modifier = Modifier.testTag("nav_tab_habits")
                            )

                            NavigationBarItem(
                                selected = currentTab == "journal",
                                onClick = { currentTab = "journal" },
                                icon = { Icon(Icons.Default.Edit, contentDescription = "Diario") },
                                label = { Text("Diario", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = CosmicSubtle,
                                    unselectedTextColor = CosmicSubtle,
                                    indicatorColor = CosmicPurple
                                ),
                                modifier = Modifier.testTag("nav_tab_journal")
                            )

                            NavigationBarItem(
                                selected = currentTab == "achievements",
                                onClick = { currentTab = "achievements" },
                                icon = { Icon(Icons.Default.Star, contentDescription = "Logros") },
                                label = { Text("Logros", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = CosmicSubtle,
                                    unselectedTextColor = CosmicSubtle,
                                    indicatorColor = CosmicPurple
                                ),
                                modifier = Modifier.testTag("nav_tab_achievements")
                            )

                            NavigationBarItem(
                                selected = currentTab == "settings",
                                onClick = { currentTab = "settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                                label = { Text("Ajustes", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = CosmicSubtle,
                                    unselectedTextColor = CosmicSubtle,
                                    indicatorColor = CosmicPurple
                                ),
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Render components depending on active selected tab
                        when (currentTab) {
                            "habits" -> {
                                HabitsScreen(
                                    viewModel = viewModel,
                                    onOpenAddHabit = { showAddHabitDialog = true }
                                )
                            }
                            "journal" -> {
                                JournalScreen(viewModel = viewModel)
                            }
                            "achievements" -> {
                                AchievementsScreen(viewModel = viewModel)
                            }
                            "settings" -> {
                                SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                // Add Habit Sheet Modal
                if (showAddHabitDialog) {
                    AddHabitDialog(
                        onDismiss = { showAddHabitDialog = false },
                        onAdd = { name, emoji, style, xp, prog, uni, min, bonus, freq ->
                            viewModel.addHabit(name, emoji, style, xp, prog, uni, min, bonus, freq)
                            showAddHabitDialog = false
                        }
                    )
                }

                // Trigger congratulations pop-up overlay when achievement conditions are first unlocked
                newlyUnlockedAchievement?.let { ach ->
                    AchievementDialog(
                        achievement = ach,
                        onDismiss = { viewModel.dismissAchievementDialog() }
                    )
                }
            }
        }
    }
}
