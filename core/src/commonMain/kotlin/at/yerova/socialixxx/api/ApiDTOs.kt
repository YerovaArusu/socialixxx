package at.yerova.socialixxx.api

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(val username: String, val passwordHash: String)

@Serializable
data class RegisterRequest(
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val department: String? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val displayName: String,
    val department: String?,
    val birthday: String? = null,
    val gender: String? = null,
    val pronouns: String? = null,
    val profilePictureUrl: String? = null
)

@Serializable
data class ChatDto(
    val id: Int,
    val partnerId: Int,
    val partnerName: String,
    val status: Int,
    val unreadCount: Int = 0,
    val lastMessage: String? = null // Wurde wieder hinzugefügt!
)

@Serializable
data class MessageDto(val id: Int, val senderId: Int, val content: String, val timestamp: String, val isRead: Boolean)

@Serializable
data class SendMessageRequest(val senderId: Int, val content: String)
@Serializable
data class CreateChatRequest(val myUserId: Int, val partnerId: Int)

@Serializable
data class MarkReadRequest(val readerId: Int)

@Serializable
data class UpdateChatStatusRequest(val requesterId: Int, val newStatus: Int)

@Serializable
data class EventDto(
    val id: Int,
    val title: String,
    val description: String?,
    val eventTime: String,
    val participantCount: Int,
    val isParticipating: Boolean
)

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String?,
    val eventTime: String,
    val creatorId: Int
)

@Serializable
data class EventActionRequest(
    val userId: Int
)