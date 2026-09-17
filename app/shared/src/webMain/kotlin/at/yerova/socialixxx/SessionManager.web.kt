package at.yerova.socialixxx

import at.yerova.socialixxx.api.UserDto
import kotlinx.browser.window
import kotlinx.serialization.json.Json

actual object SessionManager {
    private const val KEY = "socialixxx_user_session"

    actual fun saveUser(user: UserDto) {
        val json = Json.encodeToString(user)
        window.localStorage.setItem(KEY, json)
    }

    actual fun getUser(): UserDto? {
        val json = window.localStorage.getItem(KEY) ?: return null
        return try {
            Json.decodeFromString<UserDto>(json)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clearSession() {
        window.localStorage.removeItem(KEY)
    }
}