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
fun ChatsScreen(
    userId: Int, displayName: String, department: String?, profilePictureUrl: String?,
    onNavigateToEvents: () -> Unit, onNavigateToTeam: () -> Unit, onNavigateToWorkplace: () -> Unit
) {
    Scaffold(
        topBar = { NavigationTopBar("Chats", profilePictureUrl, {}, {}) },
        bottomBar = { NavigationBar(1, onNavigateToEvents, {}, onNavigateToTeam, onNavigateToWorkplace) },
        containerColor = Color(0xFFF5F3F7)
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Hier entstehen die Chats!", style = MaterialTheme.typography.headlineMedium)
        }
    }
}