package at.yerova.socialixxx

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.ui.screens.ChatsScreen
import at.yerova.socialixxx.ui.screens.EventsScreen
import at.yerova.socialixxx.ui.screens.LoginScreen
import at.yerova.socialixxx.ui.screens.RegisterScreen
import at.yerova.socialixxx.ui.screens.TeamScreen
import at.yerova.socialixxx.ui.screens.WorkplaceScreen

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val apiClient = remember { ApiClient() }

        fun navigateBottomTab(route: Any) {
            navController.navigate(route) {
                popUpTo<EventsScreen> { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        NavHost(navController = navController, startDestination = LoginScreen) {
            composable<LoginScreen> {
                LoginScreen(
                    apiClient = apiClient,
                    onNavigateToRegister = { navController.navigate(RegisterScreen) },
                    onLoginSuccess = { userDto ->
                        navController.navigate(
                            EventsScreen(userDto.id, userDto.displayName, userDto.department, userDto.profilePictureUrl)
                        ) {
                            popUpTo(LoginScreen) { inclusive = true }
                        }
                    }
                )
            }

            composable<RegisterScreen> {
                RegisterScreen(
                    apiClient,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = { navController.popBackStack() })
            }

            composable<EventsScreen> { backStackEntry ->
                val r = backStackEntry.toRoute<EventsScreen>()
                EventsScreen(
                    userId = r.userId,
                    displayName = r.displayName,
                    department = r.department,
                    profilePictureUrl = r.profilePictureUrl,
                    apiClient = apiClient,
                    onNavigateToEvents = {},
                    onNavigateToChats = {
                        navigateBottomTab(
                            ChatsScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToTeam = {
                        navigateBottomTab(
                            TeamScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToWorkplace = {
                        navigateBottomTab(
                            WorkplaceScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    }
                )
            }

            composable<ChatsScreen> { backStackEntry ->
                val r = backStackEntry.toRoute<ChatsScreen>()
                ChatsScreen(
                    r.userId, r.displayName, r.department, r.profilePictureUrl,
                    onNavigateToEvents = {
                        navigateBottomTab(
                            EventsScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToTeam = {
                        navigateBottomTab(
                            TeamScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToWorkplace = {
                        navigateBottomTab(
                            WorkplaceScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    }
                )
            }

            composable<TeamScreen> { backStackEntry ->
                val r = backStackEntry.toRoute<TeamScreen>()
                TeamScreen(
                    r.userId, r.displayName, r.department, r.profilePictureUrl,
                    onNavigateToEvents = {
                        navigateBottomTab(
                            EventsScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToChats = {
                        navigateBottomTab(
                            ChatsScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToWorkplace = {
                        navigateBottomTab(
                            WorkplaceScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    }
                )
            }

            composable<WorkplaceScreen> { backStackEntry ->
                val r = backStackEntry.toRoute<WorkplaceScreen>()
                WorkplaceScreen(
                    r.userId, r.displayName, r.department, r.profilePictureUrl,
                    onNavigateToEvents = {
                        navigateBottomTab(
                            EventsScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToChats = {
                        navigateBottomTab(
                            ChatsScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    },
                    onNavigateToTeam = {
                        navigateBottomTab(
                            TeamScreen(
                                r.userId,
                                r.displayName,
                                r.department,
                                r.profilePictureUrl
                            )
                        )
                    }
                )
            }
        }
    }
}