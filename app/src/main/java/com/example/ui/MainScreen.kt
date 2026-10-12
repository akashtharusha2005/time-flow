package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.ai.AiAssistantScreen
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.calendar.CalendarScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.focus.FocusTimerScreen
import com.example.ui.habits.GoalsHabitsScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.study.StudyPlannerScreen
import com.example.ui.tasks.TasksScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    val currentDestination by mainViewModel.currentDestination.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle system back button: if not on Home, return to Home; if drawer is open, close drawer
    BackHandler(enabled = drawerState.isOpen || currentDestination != MainDestination.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentDestination != MainDestination.HOME) {
            mainViewModel.navigateTo(MainDestination.HOME)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "TIMEFLOW",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(20.dp)
                )

                // Secondary Features in Drawer
                NavigationDrawerItem(
                    label = { Text("Dashboard (Home)") },
                    selected = currentDestination == MainDestination.HOME,
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.HOME)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Task Manager") },
                    selected = currentDestination == MainDestination.TASKS,
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.TASKS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Smart Calendar") },
                    selected = currentDestination == MainDestination.CALENDAR,
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.CALENDAR)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Focus Timer") },
                    selected = currentDestination == MainDestination.FOCUS,
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.FOCUS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Habits & Goals") },
                    selected = currentDestination == MainDestination.HABITS,
                    icon = { Icon(Icons.Default.TrackChanges, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.HABITS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Study Planner") },
                    selected = currentDestination == MainDestination.STUDY,
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.STUDY)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Productivity Analytics") },
                    selected = currentDestination == MainDestination.ANALYTICS,
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.ANALYTICS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Notes & Quick Capture") },
                    selected = currentDestination == MainDestination.NOTES,
                    icon = { Icon(Icons.Default.Note, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.NOTES)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("TIMEFLOW AI Assistant") },
                    selected = currentDestination == MainDestination.AI,
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.AI)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Settings & Account") },
                    selected = currentDestination == MainDestination.SETTINGS,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = {
                        mainViewModel.navigateTo(MainDestination.SETTINGS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentDestination) {
                                MainDestination.HOME -> "TIMEFLOW"
                                MainDestination.TASKS -> stringResource(R.string.nav_tasks)
                                MainDestination.CALENDAR -> stringResource(R.string.nav_calendar)
                                MainDestination.FOCUS -> stringResource(R.string.nav_focus)
                                MainDestination.PROFILE, MainDestination.SETTINGS -> stringResource(R.string.nav_settings)
                                MainDestination.HABITS, MainDestination.GOALS -> "Habits & Goals"
                                MainDestination.STUDY -> stringResource(R.string.nav_study)
                                MainDestination.ANALYTICS -> stringResource(R.string.nav_analytics)
                                MainDestination.NOTES -> stringResource(R.string.nav_notes)
                                MainDestination.AI -> stringResource(R.string.nav_ai)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (currentDestination != MainDestination.AI) {
                            IconButton(onClick = { mainViewModel.navigateTo(MainDestination.AI) }) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.HOME,
                        onClick = { mainViewModel.navigateTo(MainDestination.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(stringResource(R.string.nav_home)) },
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.TASKS,
                        onClick = { mainViewModel.navigateTo(MainDestination.TASKS) },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = "Tasks") },
                        label = { Text(stringResource(R.string.nav_tasks)) },
                        modifier = Modifier.testTag("nav_tasks")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.CALENDAR,
                        onClick = { mainViewModel.navigateTo(MainDestination.CALENDAR) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                        label = { Text(stringResource(R.string.nav_calendar)) },
                        modifier = Modifier.testTag("nav_calendar")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.FOCUS,
                        onClick = { mainViewModel.navigateTo(MainDestination.FOCUS) },
                        icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
                        label = { Text(stringResource(R.string.nav_focus)) },
                        modifier = Modifier.testTag("nav_focus")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.SETTINGS,
                        onClick = { mainViewModel.navigateTo(MainDestination.SETTINGS) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text(stringResource(R.string.nav_profile)) },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    MainDestination.HOME -> DashboardScreen(
                        mainViewModel = mainViewModel,
                        onNavigateToTasks = { mainViewModel.navigateTo(MainDestination.TASKS) },
                        onNavigateToFocus = { mainViewModel.navigateTo(MainDestination.FOCUS) },
                        onNavigateToCalendar = { mainViewModel.navigateTo(MainDestination.CALENDAR) },
                        onNavigateToStudy = { mainViewModel.navigateTo(MainDestination.STUDY) },
                        onNavigateToAi = { mainViewModel.navigateTo(MainDestination.AI) }
                    )
                    MainDestination.TASKS -> TasksScreen(mainViewModel = mainViewModel)
                    MainDestination.CALENDAR -> CalendarScreen(mainViewModel = mainViewModel)
                    MainDestination.FOCUS -> FocusTimerScreen(mainViewModel = mainViewModel)
                    MainDestination.HABITS, MainDestination.GOALS -> GoalsHabitsScreen(mainViewModel = mainViewModel)
                    MainDestination.STUDY -> StudyPlannerScreen(mainViewModel = mainViewModel)
                    MainDestination.ANALYTICS -> AnalyticsScreen(mainViewModel = mainViewModel)
                    MainDestination.NOTES -> NotesScreen(mainViewModel = mainViewModel)
                    MainDestination.AI -> AiAssistantScreen(mainViewModel = mainViewModel)
                    MainDestination.SETTINGS, MainDestination.PROFILE -> SettingsScreen(
                        mainViewModel = mainViewModel,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}
