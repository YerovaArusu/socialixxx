package at.yerova.socialixxx

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.ui.EventsScreen
import at.yerova.socialixxx.ui.LoginScreen
import at.yerova.socialixxx.ui.RegisterScreen

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        val apiClient = remember { ApiClient() }

        NavHost(
            navController = navController,
            startDestination = LoginScreen
        ) {

            composable<LoginScreen> {
                LoginScreen(
                    apiClient = apiClient,
                    onNavigateToRegister = {
                        navController.navigate(RegisterScreen)
                    },
                    onLoginSuccess = { userDto ->
                        navController.navigate(
                            EventsScreen(
                                userId = userDto.id,
                                displayName = userDto.displayName,
                                department = userDto.department
                            )
                        ) {
                            popUpTo(LoginScreen) { inclusive = true }
                        }
                    }
                )
            }

            composable<RegisterScreen> {
                RegisterScreen(
                    apiClient = apiClient,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable<EventsScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<EventsScreen>()

                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Willkommen zurück, ${route.displayName}!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (route.department != null) {
                        Text(text = "Abteilung: ${route.department}")
                    }
                    Text(text = "(User ID: ${route.userId})")
                }
            }
        }
    }
}