package ci.nsu.moble.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ci.nsu.moble.main.ui.theme.PracticeTheme

// Sealed class для маршрутов
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Главная")
    object ScreenOne : Screen("screen_one", "Экран 1")
    object ScreenTwo : Screen("screen_two", "Экран 2")
}

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedMessage = intent.getStringExtra("USER_MESSAGE") ?: "Нет сообщения"

        setContent {
            PracticeTheme {
                SecondActivityScreen(
                    message = receivedMessage,
                    onNavigateBack = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondActivityScreen(
    message: String = "",
    onNavigateBack: () -> Unit = {}
) {
    // NavController для управления навигацией
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(Screen.Home, Screen.ScreenOne, Screen.ScreenTwo)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Передано: $message") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Home -> Icon(Icons.Filled.Home, contentDescription = "Home")
                                Screen.ScreenOne -> Icon(Icons.Filled.List, contentDescription = "Screen One")
                                Screen.ScreenTwo -> Icon(Icons.Filled.Settings, contentDescription = "Screen Two")
                            }
                        },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            // Навигация с сохранением состояния
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost - контейнер для навигации
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Регистрируем каждый экран
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToScreenOne = {
                        navController.navigate(Screen.ScreenOne.route)
                    }
                )
            }

            composable(Screen.ScreenOne.route) {
                ScreenOneScreen(
                    onNavigateBack = {
                        navController.popBackStack() // Возврат на предыдущий экран
                    },
                    onNavigateToScreenTwo = {
                        navController.navigate(Screen.ScreenTwo.route)
                    }
                )
            }

            composable(Screen.ScreenTwo.route) {
                ScreenTwoScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Экран "Главная"
@Composable
fun HomeScreen(onNavigateToScreenOne: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Главный экран",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onNavigateToScreenOne,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Перейти на Экран 1!@")
            }
        }
    }
}

// Экран 1
@Composable
fun ScreenOneScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScreenTwo: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Экран 1",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Назад")
            }
            Button(
                onClick = onNavigateToScreenTwo,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Далее")
            }
        }
    }
}

// Экран 2
@Composable
fun ScreenTwoScreen(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Экран 2",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Вернуться")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PracticeTheme {
        HomeScreen(onNavigateToScreenOne = {})
    }
}