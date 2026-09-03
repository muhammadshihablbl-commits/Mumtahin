package com.mumtahin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mumtahin.ui.screens.HomeScreen
import com.mumtahin.ui.screens.SettingsScreen
import com.mumtahin.ui.screens.SubjectScreen
import com.mumtahin.ui.theme.MumtahinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MumtahinTheme {
                MumtahinApp()
            }
        }
    }
}

/**
 * App navigation destinations.
 * Home and Settings are top-level tabs shown in the bottom bar.
 * Subject is a drill-down screen opened by tapping a subject on Home —
 * it hides the bottom bar and shows its own toolbar instead.
 */
private sealed class AppScreen {
    object Home : AppScreen()
    object Settings : AppScreen()
    data class Subject(val name: String) : AppScreen()
}

@Composable
fun MumtahinApp() {
    var currentScreen: AppScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen !is AppScreen.Subject) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Home,
                        onClick = { currentScreen = AppScreen.Home },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.Home) {
                                    Icons.Filled.Home
                                } else {
                                    Icons.Outlined.Home
                                },
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Settings,
                        onClick = { currentScreen = AppScreen.Settings },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.Settings) {
                                    Icons.Filled.Settings
                                } else {
                                    Icons.Outlined.Settings
                                },
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (val screen = currentScreen) {
            AppScreen.Home -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onSubjectClick = { subjectName ->
                    currentScreen = AppScreen.Subject(subjectName)
                }
            )
            AppScreen.Settings -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            is AppScreen.Subject -> SubjectScreen(
                subjectName = screen.name,
                onBackClick = { currentScreen = AppScreen.Home },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
