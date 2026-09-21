package at.yerova.socialixxx.ui.screens

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("login")
object LoginScreen

@Serializable
@SerialName("register")
object RegisterScreen

@Serializable
@SerialName("chats")
object ChatsScreenRoute

@Serializable
@SerialName("chat")
data class ChatDetailScreenRoute(
    val chatId: Int
)

@Serializable
@SerialName("user")
data class UserProfileScreenRoute(val targetUserId: Int)

@Serializable
@SerialName("spaces")
object SpacesScreenRoute


@Serializable
@SerialName("space")
data class SpaceDetailRoute(
    val spaceId: Int,
    val spaceName: String,
    val isAssigned: Boolean
)

@Serializable
data class SpacePostDetailRoute(
    val spaceId: Int,
    val postId: Int,
    val isAssigned: Boolean
)

@Serializable
@SerialName("workplace")
object WorkplaceScreenRoute

@Serializable
@SerialName("events")
object EventsScreenRoute

@Serializable
@SerialName("event_details")
data class EventDetailRoute(val eventId: Int)