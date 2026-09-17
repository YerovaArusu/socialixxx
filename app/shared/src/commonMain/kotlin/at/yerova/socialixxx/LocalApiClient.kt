package at.yerova.socialixxx

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.api.UserDto

val LocalApiClient = staticCompositionLocalOf<ApiClient> {
    error("ApiClient wurde nicht initialisiert!")
}

val LocalUser = compositionLocalOf<UserDto?> { null }