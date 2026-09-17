package at.yerova.socialixxx

import at.yerova.socialixxx.api.UserDto

actual object SessionManager {
    private var memoryUser: UserDto? = null

    actual fun saveUser(user: UserDto) {
        memoryUser = user
    }

    actual fun getUser(): UserDto? {
        return memoryUser
    }

    actual fun clearSession() {
        memoryUser = null
    }
}