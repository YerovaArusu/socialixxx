package at.yerova.socialixxx

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.compose.rememberNavController
import kotlinx.browser.document

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class,
)
fun main() {
    val body = document.body ?: return

    ComposeViewport(body) {
        val navController = rememberNavController()

        LaunchedEffect(navController) {
            navController.bindToBrowserNavigation()
        }

        App(navController = navController)
    }
}