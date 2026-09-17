package at.yerova.socialixxx

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import at.yerova.socialixxx.ui.screens.*

@Composable
fun App(
    navController: NavHostController = rememberNavController()
) {
    MaterialTheme {
        fun navigateBottomTab(route: Any) {
            navController.navigate(route) {
                popUpTo<EventsScreen> { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        val apiClient = remember { ApiClient() }
        var currentUser by remember { mutableStateOf(SessionManager.getUser()) }

        CompositionLocalProvider(
            LocalApiClient provides apiClient, LocalUser provides currentUser
        ) {
            NavHost(navController = navController, startDestination = LoginScreen) {

                composable<LoginScreen> {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate(RegisterScreen) },
                        onLoginSuccess = { userDto ->
                            SessionManager.saveUser(userDto)
                            currentUser = userDto
                            navController.navigate(EventsScreen) {
                                popUpTo(LoginScreen) { inclusive = true }
                            }
                        })
                }

                composable<RegisterScreen> {
                    RegisterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onRegisterSuccess = { navController.popBackStack() })
                }

                composable<EventsScreen> {
                    EventsScreen(
                        onNavigateToEventDetail = { eventId -> navController.navigate(EventDetailRoute(eventId)) },
                        onNavigateToEvents = {}, // Sind wir schon
                        onNavigateToChats = { navigateBottomTab(ChatsScreen) },
                        onNavigateToTeam = { navigateBottomTab(TeamScreen) },
                        onNavigateToWorkplace = { navigateBottomTab(WorkplaceScreen) })
                }

                composable<ChatsScreen> {
                    ChatsScreen(
                        onNavigateToChatDetail = { chatId, _, _ -> navController.navigate(ChatDetailRoute(chatId)) },
                        onNavigateToUserProfile = { targetUserId -> navController.navigate(UserProfileRoute(targetUserId)) },
                        onNavigateToEvents = { navigateBottomTab(EventsScreen) },
                        onNavigateToTeam = { navigateBottomTab(TeamScreen) },
                        onNavigateToWorkplace = { navigateBottomTab(WorkplaceScreen) })
                }

                composable<TeamScreen> {
                    TeamScreen(
                        onNavigateToEvents = { navigateBottomTab(EventsScreen) },
                        onNavigateToChats = { navigateBottomTab(ChatsScreen) },
                        onNavigateToWorkplace = { navigateBottomTab(WorkplaceScreen) })
                }

                composable<WorkplaceScreen> {
                    WorkplaceScreen(
                        onNavigateToEvents = { navigateBottomTab(EventsScreen) },
                        onNavigateToChats = { navigateBottomTab(ChatsScreen) },
                        onNavigateToTeam = { navigateBottomTab(TeamScreen) },
                    )
                }

                composable<EventDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<EventDetailRoute>()

                    EventDetailScreen(
                        eventId = route.eventId, onNavigateBack = { navController.popBackStack() })
                }

                composable<ChatDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatDetailRoute>()

                    ChatDetailScreen(
                        chatId = route.chatId, onNavigateBack = { navController.popBackStack() })
                }

                composable<UserProfileRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<UserProfileRoute>()

                    Scaffold(
                        topBar = {
                            TopAppBar(title = { Text("Profil") }, navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    val iconFont = getMaterialSymbolsFont()
                                    Text(
                                        text = "arrow_back",
                                        fontFamily = iconFont,
                                        fontSize = 32.sp,
                                        color = Color.Black
                                    )
                                }
                            })
                        }) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Text("Infos für UserID: ${SessionManager.getUser()?.id}")
                        }
                    }
                }
            }
        }
    }
}