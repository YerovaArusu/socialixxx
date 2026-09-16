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
fun WorkplaceScreen(
    userId: Int, displayName: String, department: String?, profilePictureUrl: String?,
    onNavigateToEvents: () -> Unit, onNavigateToChats: () -> Unit, onNavigateToTeam: () -> Unit
) {
    Scaffold(
        topBar = { NavigationTopBar("Workplace", profilePictureUrl, {}, {}) },
        bottomBar = { NavigationBar(3, onNavigateToEvents, onNavigateToChats, onNavigateToTeam, {}) },
        containerColor = Color(0xFFF5F3F7)
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Hier entstehen die Einstellungen!", style = MaterialTheme.typography.headlineMedium)
        }
    }
}