package at.yerova.socialixxx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.api.EventDto
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.ui.getMaterialSymbolsFont

@Composable
fun EventsScreen(
    userId: Int,
    displayName: String,
    department: String?,
    profilePictureUrl: String?,
    apiClient: ApiClient,
    onNavigateToEvents: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToWorkplace: () -> Unit
) {
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        val result = apiClient.getEvents(userId)
        isLoading = false
        when (result) {
            is NetworkResult.Success -> events = result.data
            is NetworkResult.Error -> errorMessage = result.message
        }
    }

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = "Ereignisse",
                profilePictureUrl = profilePictureUrl,
                onAddClick = {
                    println("Neues Event erstellen geklickt!")
                },
                onProfileClick = {
                    println("Profil geklickt!")
                }
            )
        },
        bottomBar = {
            NavigationBar(
                currentTab = 0, // 0 = Events/Kalender
                onNavigateToEvents = onNavigateToEvents,
                onNavigateToChats = onNavigateToChats,
                onNavigateToTeam = onNavigateToTeam,
                onNavigateToWorkplace = onNavigateToWorkplace
            )
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
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
                        EventCard(event = event)
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: EventDto) {
    val iconFont = getMaterialSymbolsFont()
    val timeString = event.eventTime.substringAfter("T").take(5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.width(60.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = timeString, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
                    .background(Color.Black)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description ?: "Keine Beschreibung.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Participants", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "group",
                            fontFamily = iconFont,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = event.participantCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}