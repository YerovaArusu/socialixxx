package at.yerova.socialixxx

import at.yerova.socialixxx.api.UserDto

expect object SessionManager {
    fun saveUser(user: UserDto)
    fun getUser(): UserDto?
    fun clearSession()
}