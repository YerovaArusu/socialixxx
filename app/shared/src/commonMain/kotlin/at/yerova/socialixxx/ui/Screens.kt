package at.yerova.socialixxx.ui

import kotlinx.serialization.Serializable

// Einfache Screens ohne Parameter sind 'objects'
@Serializable
object LoginScreen

@Serializable
object RegisterScreen

@Serializable
data class EventsScreen(
    val userId: Int,
    val displayName: String,
    val department: String?
)