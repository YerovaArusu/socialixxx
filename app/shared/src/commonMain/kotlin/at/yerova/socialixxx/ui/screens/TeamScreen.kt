package at.yerova.socialixxx.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun TeamScreen(
    userId: Int, displayName: String, department: String?, profilePictureUrl: String?,
    onNavigateToEvents: () -> Unit, onNavigateToChats: () -> Unit, onNavigateToWorkplace: () -> Unit
) {
    Scaffold(
        topBar = { NavigationTopBar("Team", profilePictureUrl, {}, {}) },
        bottomBar = { NavigationBar(2, onNavigateToEvents, onNavigateToChats, {}, onNavigateToWorkplace) },
        containerColor = Color(0xFFF5F3F7)
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Hier entsteht das Team-Directory!", style = MaterialTheme.typography.headlineMedium)
        }
    }
}