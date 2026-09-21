package at.yerova.socialixxx

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.ui.screens.*
import at.yerova.socialixxx.ui.screens.chats.ChatDetailScreen
import at.yerova.socialixxx.ui.screens.chats.ChatsScreen
import at.yerova.socialixxx.ui.screens.events.EventDetailScreen
import at.yerova.socialixxx.ui.screens.events.EventsScreen
import at.yerova.socialixxx.ui.screens.generic.LoginScreen
import at.yerova.socialixxx.ui.screens.generic.RegisterScreen
import at.yerova.socialixxx.ui.screens.generic.UserProfileScreen
import at.yerova.socialixxx.ui.screens.workplaces.WorkplaceScreen
import at.yerova.socialixxx.ui.screens.spaces.SpaceDetailScreen
import at.yerova.socialixxx.ui.screens.spaces.SpacePostDetailScreen
import at.yerova.socialixxx.ui.screens.spaces.SpacesScreen

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
                        onNavigateToTeam = { navigateBottomTab(SpacesScreen) },
                        onNavigateToWorkplace = { navigateBottomTab(WorkplaceScreen) })
                }

                composable<ChatsScreen> {
                    ChatsScreen(
                        onNavigateToChatDetail = { chatId, _, _ -> navController.navigate(ChatDetailScreen(chatId)) },
                        onNavigateToUserProfile = { targetUserId ->
                            navController.navigate(
                                UserProfileScreen(
                                    targetUserId
                                )
                            )
                        },
                        onNavigateToEvents = { navigateBottomTab(EventsScreen) },
                        onNavigateToTeam = { navigateBottomTab(SpacesScreen) },
                        onNavigateToWorkplace = { navigateBottomTab(WorkplaceScreen) })
                }

                composable<SpacesScreen> {
                    SpacesScreen(
                        onNavigateToSpaceDetail = { spaceId, spaceName, isAssigned ->
                            navController.navigate(SpaceDetailRoute(spaceId, spaceName, isAssigned))
                        },
                        onNavigateToEvents = { navigateBottomTab(EventsScreen) },
                        onNavigateToChats = { navigateBottomTab(ChatsScreen) },
                        onNavigateToWorkplace = { navigateBottomTab(WorkplaceScreen) },
                        onNavigateToUserProfile = { targetUserId ->
                            navController.navigate(
                                UserProfileScreen(
                                    targetUserId
                                )
                            )
                        }
                    )
                }

                composable<SpaceDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SpaceDetailRoute>()
                    SpaceDetailScreen(
                        spaceId = route.spaceId,
                        spaceName = route.spaceName,
                        isAssigned = route.isAssigned,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPostDetail = { postId ->
                            navController.navigate(SpacePostDetailRoute(route.spaceId, postId, route.isAssigned))
                        }
                    )
                }

                composable<SpacePostDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SpacePostDetailRoute>()
                    SpacePostDetailScreen(
                        spaceId = route.spaceId,
                        postId = route.postId,
                        isAssigned = route.isAssigned,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<WorkplaceScreen> {
                    WorkplaceScreen(
                        onNavigateToEvents = { navigateBottomTab(EventsScreen) },
                        onNavigateToChats = { navigateBottomTab(ChatsScreen) },
                        onNavigateToTeam = { navigateBottomTab(SpacesScreen) },
                    )
                }

                composable<EventDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<EventDetailRoute>()

                    EventDetailScreen(
                        eventId = route.eventId, onNavigateBack = { navController.popBackStack() })
                }

                composable<ChatDetailScreen> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatDetailScreen>()

                    ChatDetailScreen(
                        chatId = route.chatId,
                        onNavigateToUserProfile = { uId -> navController.navigate(UserProfileScreen(uId)) },
                        onNavigateBack = { navController.popBackStack() })
                }

                composable<UserProfileScreen> { backStackEntry ->
                    val route = backStackEntry.toRoute<UserProfileScreen>()

                    UserProfileScreen(
                        targetUserId = route.targetUserId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}