package at.yerova.socialixxx

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
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
import at.yerova.socialixxx.ui.screens.generic.*
import at.yerova.socialixxx.ui.screens.spaces.SpaceDetailScreen
import at.yerova.socialixxx.ui.screens.spaces.SpacePostDetailScreen
import at.yerova.socialixxx.ui.screens.spaces.SpacesScreen
import at.yerova.socialixxx.ui.screens.workplaces.WorkplaceScreen
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import socialixxx.app.shared.generated.resources.Res
import socialixxx.app.shared.generated.resources.material_symbols_outlined
import kotlin.time.Duration.Companion.seconds

@Composable
fun App(
    navController: NavHostController = rememberNavController()
) {
    MaterialTheme {

        val apiClient = remember { ApiClient() }
        var currentUser by remember { mutableStateOf(SessionManager.getUser()) }

        var serverState by remember { mutableStateOf(ServerState.CHECKING) }

        // For init purpose. To load the font before trying to display the font
        val symbolFont = FontFamily(Font(Res.font.material_symbols_outlined))

        LaunchedEffect(Unit) {
            while (true) {
                val isOnline = apiClient.pingServer()
                serverState = if (isOnline) ServerState.ONLINE else ServerState.OFFLINE
                delay(3.seconds)
            }
        }

        CompositionLocalProvider(
            LocalApiClient provides apiClient,
            LocalUser provides currentUser,
            LocalNavController provides navController,
            LocalSymbolFont provides symbolFont
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(navController = navController, startDestination = LoginScreen) {

                    composable<LoginScreen> {
                        LoginScreen(
                            onNavigateToRegister = { navController.navigate(RegisterScreen) },
                            onLoginSuccess = { userDto ->
                                SessionManager.saveUser(userDto)
                                currentUser = userDto
                                navController.navigate(EventsScreenRoute) {
                                    popUpTo(LoginScreen) { inclusive = true }
                                }
                            })
                    }

                    composable<RegisterScreen> {
                        RegisterScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onRegisterSuccess = { navController.popBackStack() })
                    }

                    composable<EventsScreenRoute> {
                        EventsScreen(
                            onNavigateToEventDetail = { eventId -> navController.navigate(EventDetailRoute(eventId)) })
                    }

                    composable<ChatsScreenRoute> {
                        ChatsScreen()
                    }

                    composable<SpacesScreenRoute> {
                        SpacesScreen(
                            onNavigateToSpaceDetail = { spaceId, spaceName, isAssigned ->
                                navController.navigate(SpaceDetailRoute(spaceId, spaceName, isAssigned))
                            })
                    }

                    composable<SpaceDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<SpaceDetailRoute>()
                        SpaceDetailScreen(
                            spaceId = route.spaceId,
                            spaceName = route.spaceName,
                            isAssigned = route.isAssigned,
                            onNavigateToPostDetail = { postId ->
                                navController.navigate(SpacePostDetailRoute(route.spaceId, postId, route.isAssigned))
                            })
                    }

                    composable<SpacePostDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<SpacePostDetailRoute>()
                        SpacePostDetailScreen(
                            spaceId = route.spaceId,
                            postId = route.postId,
                            isAssigned = route.isAssigned,
                        )
                    }

                    composable<WorkplaceScreenRoute> {
                        WorkplaceScreen(
                        )
                    }

                    composable<EventDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<EventDetailRoute>()

                        EventDetailScreen(
                            eventId = route.eventId
                        )
                    }

                    composable<ChatDetailScreenRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<ChatDetailScreenRoute>()

                        ChatDetailScreen(
                            chatId = route.chatId,
                        )
                    }

                    composable<UserProfileScreenRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<UserProfileScreenRoute>()

                        UserProfileScreen(
                            targetUserId = route.targetUserId,
                        )
                    }
                }

                if (serverState != ServerState.ONLINE) {
                    ConnectingScreen(state = serverState)
                }
            }
        }
    }
}