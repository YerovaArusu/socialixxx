package at.yerova.socialixxx.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.EventActionRequest
import at.yerova.socialixxx.api.EventDto
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    onNavigateBack: () -> Unit
) {
    var event by remember { mutableStateOf<EventDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isJoining by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val iconFont = getMaterialSymbolsFont()

    val scope = rememberCoroutineScope()

    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return


    LaunchedEffect(refreshTrigger) {
        val result = apiClient.getEvent(eventId, currentUser.id)
        isLoading = false
        println("Refetching event with id $eventId. Is: ${result is NetworkResult.Success}")
        if (result is NetworkResult.Success) {
            event = result.data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    Box(modifier = Modifier.onClick { onNavigateBack() }) {
                        Text(
                            text = "arrow_back",
                            fontFamily = iconFont,
                            fontSize = 36.sp,
                            color = Color.Black
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F3F7)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (event == null) {
                Text("Event nicht gefunden.", modifier = Modifier.align(Alignment.Center))
            } else {
                val e = event!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(e.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wann: ${e.eventTime.replace("T", " um ")} Uhr", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(e.description ?: "Keine Beschreibung hinterlegt.", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(32.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Teilnehmer: ${e.participantCount}", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.weight(1f)) // Schiebt den Button nach ganz unten

                    Button(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !e.isParticipating && !isJoining,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (e.isParticipating) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            scope.launch {
                                isJoining = true
                                val result = apiClient.joinEvent(e.id, EventActionRequest(currentUser.id))
                                isJoining = false
                                if (result is NetworkResult.Success) refreshTrigger++
                            }
                        }
                    ) {
                        if (isJoining) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else if (e.isParticipating) {
                            Text("Du nimmst teil!")
                        } else {
                            Text("Event beitreten")
                        }
                    }
                }
            }
        }
    }
}