package at.yerova.socialixxx.api

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(val username: String, val passwordHash: String)

@Serializable
data class DepartmentBaseDto(val id: Int, val name: String, val kz: String)

@Serializable
data class RegisterRequest(
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val departmentId: Int? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val displayName: String,
    val birthday: String?,
    val gender: String?,
    val pronouns: String?,
    val profilePictureUrl: String?,
    val dw: String? = null,
    val kz: String? = null,
    val lehrjahr: Int? = null,
    val hasActiveStory: Boolean = false
)

@Serializable
data class StoryDto(
    val id: Int,
    val userId: Int,
    val mediaUrl: String,
    val caption: String?,
    val timestamp: String
)

@Serializable
data class CreateStoryRequest(
    val userId: Int,
    val mediaUrl: String,
    val caption: String?
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
    val isParticipating: Boolean,
    val commentCount: Int = 0
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

@Serializable
data class EventCommentDto(
    val id: Int,
    val userId: Int,
    val userName: String,
    val userProfilePic: String?,
    val content: String,
    val timestamp: String
)

@Serializable
data class CreateEventCommentRequest(
    val eventId: Int,
    val userId: Int,
    val content: String
)

@Serializable
data class SpaceDto(
    val id: Int,
    val name: String,
    val kz: String, // Kurzzeichen, z.B. "IT"
    val description: String?,
    val isAssigned: Boolean // True = Schreibrechte, False = Read-Only
)

@Serializable
data class SpacePostDto(
    val id: Int,
    val departmentId: Int,
    val authorId: Int,
    val authorName: String, // Direkt mitgeliefert für schnelles UI-Rendering
    val authorProfilePic: String?,
    val content: String,
    val mediaUrl: String?,
    val timestamp: String,
    val commentCount: Int = 0 // Zeigt an, ob unter dem Post diskutiert wird
)

@Serializable
data class SpacePostCommentDto(
    val id: Int,
    val postId: Int,
    val authorId: Int,
    val authorName: String,
    val authorProfilePic: String?,
    val content: String,
    val timestamp: String
)

@Serializable
data class QuestionDto(
    val id: Int,
    val departmentId: Int,
    val authorId: Int,
    val authorName: String, // Wer hat die Frage/Antwort erstellt?
    val questionTitle: String,
    val answerText: String,
    val timestamp: String
)

@Serializable
data class CreateSpacePostRequest(
    val departmentId: Int,
    val authorId: Int,
    val content: String,
    val mediaUrl: String? = null
)

@Serializable
data class CreateSpacePostCommentRequest(
    val postId: Int,
    val authorId: Int,
    val content: String
)

@Serializable
data class CreateQuestionRequest(
    val departmentId: Int,
    val authorId: Int,
    val questionTitle: String,
    val answerText: String
)