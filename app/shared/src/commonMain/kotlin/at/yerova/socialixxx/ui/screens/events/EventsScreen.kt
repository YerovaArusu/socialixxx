package at.yerova.socialixxx.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalSymbolFont
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.api.CreateEventRequest
import at.yerova.socialixxx.api.EventDto
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.ui.NavigationBar
import at.yerova.socialixxx.ui.NavigationTopBar
import kotlinx.coroutines.launch

@Composable
fun EventsScreen(
    onNavigateToEventDetail: (Int) -> Unit,
) {
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return


    LaunchedEffect(refreshTrigger) {
        isLoading = true
        val result = apiClient.getEvents(currentUser.id)
        isLoading = false
        when (result) {
            is NetworkResult.Success -> events = result.data
            is NetworkResult.Error -> errorMessage = result.message
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            userId = currentUser.id,
            apiClient = apiClient,
            onDismiss = { showCreateDialog = false },
            onSuccess = {
                showCreateDialog = false
                refreshTrigger++
            }
        )
    }

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = "Ereignisse",
                onAddClick = { showCreateDialog = true },
            )
        },
        bottomBar = {
            NavigationBar(
                currentTab = 0
            )
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && events.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events) { event ->
                        EventCard(
                            event = event,
                            onClick = { onNavigateToEventDetail(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: EventDto, onClick: () -> Unit) {
    val iconFont = LocalSymbolFont.current
    val timeString = event.eventTime.substringAfter("T").take(5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.Start) {
                Text(text = "Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = timeString, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(1.dp).height(80.dp).background(Color.Black))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = event.description ?: "Keine Beschreibung.", fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Interaktionen", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "group", fontFamily = iconFont, fontSize = 16.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = event.participantCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(text = "chat_bubble", fontFamily = iconFont, fontSize = 16.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = event.commentCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEventDialog(
    userId: Int,
    apiClient: ApiClient,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-09-17") } // Platzhalter für Morgen
    var time by remember { mutableStateOf("14:00") }

    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neues Event erstellen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung (Optional)") })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Datum (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Zeit (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && title.isNotBlank() && date.isNotBlank() && time.isNotBlank(),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null
                        val isoTime = "${date.trim()}T${time.trim()}:00"
                        val req = CreateEventRequest(title, description.takeIf { it.isNotBlank() }, isoTime, userId)

                        when (val res = apiClient.createEvent(req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> {
                                error = res.message; isSubmitting = false
                            }
                        }
                    }
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Erstellen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}