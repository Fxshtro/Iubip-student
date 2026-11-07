package ru.iubip.student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.iubip.student.navigation.BottomDestination
import ru.iubip.student.navigation.Routes
import ru.iubip.student.ui.components.IubipBottomBar
import ru.iubip.student.ui.screens.profile.ProfileScreen
import ru.iubip.student.ui.screens.schedule.ScheduleScreen
import ru.iubip.student.ui.theme.IubipAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            IubipAppTheme {
                IubipApp()
            }
        }
    }
}

/**
 * Корневой Composable: Scaffold с нижней панелью и NavHost для двух разделов.
 *
 * Фон — [MaterialTheme.colorScheme.background] (#F7F8FA светлая), панель без заливки.
 */
@Composable
fun IubipApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = BottomDestination.entries
        .firstOrNull { destination ->
            currentDestination?.hierarchy?.any { it.route == destination.route } == true
        }
        ?.route

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            IubipBottomBar(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        // Один экран старта в стеке + сохранение состояния разделов.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SCHEDULE,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Routes.SCHEDULE) { ScheduleScreen() }
            composable(Routes.PROFILE) { ProfileScreen() }
        }
    }
}
