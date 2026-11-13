package ru.iubip.student

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.iubip.student.navigation.BottomDestination
import ru.iubip.student.navigation.Routes
import ru.iubip.student.ui.components.IubipBottomBar
import ru.iubip.student.ui.components.IubipSideBar
import ru.iubip.student.ui.screens.profile.ProfileScreen
import ru.iubip.student.ui.components.toIubipWindowInfo
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
 * Корневой Composable: Scaffold с панелью навигации и NavHost для двух разделов.
 *
 * Панель выбирается по ШИРИНЕ окна (`WindowSizeClass`), а не по ориентации:
 * - < 600 dp (телефон в портрете) — нижняя панель [IubipBottomBar], как раньше;
 * - ≥ 600 dp (телефон в горизонтали, планшет, разделённое окно) — вертикальная
 *   панель [IubipSideBar] по правому краю.
 *
 * Безопасные зоны (вырез под камеру-каплю, «остров», системные панели) отступаются
 * ОДИН раз здесь через `WindowInsets.safeDrawing`: и панель, и контент получают
 * этот отступ, поэтому в горизонтали ничего не залезает под вырез ни слева, ни справа.
 * `safeDrawing` включает и `displayCutout`, и системные панели (в горизонтали
 * Android рисует полосу навигации сбоку).
 *
 * Фон — [MaterialTheme.colorScheme.background] (#F7F8FA светлая), панель без заливки.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun IubipApp() {
    val context = LocalContext.current
    // Единственная точка, где размер окна превращается в класс ширины.
    val widthSizeClass = if (context is Activity) {
        calculateWindowSizeClass(context).widthSizeClass
    } else {
        // Превью/тесты без Activity: телефон в портрете по умолчанию.
        WindowWidthSizeClass.Compact
    }
    IubipAppContent(windowWidthSizeClass = widthSizeClass)
}

/**
 * Каркас приложения с уже посчитанным классом размера окна — удобно для превью.
 *
 * @param windowWidthSizeClass класс ширины окна: от него зависит панель навигации
 */
@Composable
fun IubipAppContent(windowWidthSizeClass: WindowWidthSizeClass) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = BottomDestination.entries
        .firstOrNull { destination ->
            currentDestination?.hierarchy?.any { it.route == destination.route } == true
        }
        ?.route
    val window = windowWidthSizeClass.toIubipWindowInfo()

    val onNavigate: (BottomDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            // Один экран старта в стеке + сохранение состояния разделов.
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navHost: @Composable (Modifier) -> Unit = { navModifier ->
        NavHost(
            navController = navController,
            startDestination = Routes.SCHEDULE,
            modifier = navModifier,
        ) {
            composable(Routes.SCHEDULE) { ScheduleScreen(windowWidthSizeClass = windowWidthSizeClass) }
            composable(Routes.PROFILE) { ProfileScreen(windowWidthSizeClass = windowWidthSizeClass) }
        }
    }

    if (window.isWide) {
        // Широкое окно: контент занимает всё, панель — отдельной колонкой справа.
        //
        // Инсеты применяются РАЗДЕЛЬНО: у контента — полные `safeDrawing` (вырез, статус-бар,
        // навигация), у панели — только её собственный край через `windowInsetsPadding`
        // на IubipSideBar. Если подложить общий отступ всей строке, панель в горизонтали
        // упирается в системную панель навигации снизу и выглядит обрезанной снизу.
        Row(modifier = Modifier.fillMaxSize()) {
            navHost(
                Modifier
                    .weight(1f)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            )
            IubipSideBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            )
        }
    } else {
        Scaffold(
            // Инсеты раздаются вручную: контенту — safeDrawing, панели — свой край.
            // Раньше отступ стоял на самом Scaffold и панель считала его ещё раз,
            // из-за чего в горизонтали снизу не оставалось воздуха до системной полосы.
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                IubipBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                )
            },
        ) { innerPadding ->
            navHost(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            )
        }
    }
}
