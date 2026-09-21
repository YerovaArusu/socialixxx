package at.yerova.socialixxx.ui.screens.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalSymbolFont

enum class ServerState { CHECKING, ONLINE, OFFLINE }

@Composable
fun ConnectingScreen(state: ServerState) {
    val iconFont = LocalSymbolFont.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3F7))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state == ServerState.CHECKING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Suche Server...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            } else if (state == ServerState.OFFLINE) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFFFEBEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "wifi_off", fontFamily = iconFont, fontSize = 40.sp, color = Color(0xFFD32F2F))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Verbindung verloren",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Wir versuchen, dich neu zu verbinden...",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFFD32F2F),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}