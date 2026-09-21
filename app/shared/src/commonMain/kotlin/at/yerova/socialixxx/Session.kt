package at.yerova.socialixxx

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavHostController
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.api.UserDto
import org.jetbrains.compose.resources.Font
import socialixxx.app.shared.generated.resources.Res
import socialixxx.app.shared.generated.resources.material_symbols_outlined

val LocalApiClient = staticCompositionLocalOf<ApiClient> {
    error("ApiClient wurde nicht initialisiert!")
}

val LocalUser = compositionLocalOf<UserDto?> { null }

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

val LocalSymbolFont = staticCompositionLocalOf<FontFamily> {
    error("No Font family provided")
}