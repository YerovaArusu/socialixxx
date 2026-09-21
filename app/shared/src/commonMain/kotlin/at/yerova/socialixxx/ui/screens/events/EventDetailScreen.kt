package at.yerova.socialixxx.ui.screens.events

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.CreateEventCommentRequest
import at.yerova.socialixxx.api.EventActionRequest
import at.yerova.socialixxx.api.EventCommentDto
import at.yerova.socialixxx.api.EventDto
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    onNavigateBack: () -> Unit
) {
    var event by remember { mutableStateOf<EventDto?>(null) }
    var comments by remember { mutableStateOf<List<EventCommentDto>>(emptyList()) } // NEU
    var newCommentText by remember { mutableStateOf("") } // NEU

    var isLoading by remember { mutableStateOf(true) }
    var isJoining by remember { mutableStateOf(false) }
    var isPostingComment by remember { mutableStateOf(false) } // NEU
    var refreshTrigger by remember { mutableStateOf(0) }

    val iconFont = getMaterialSymbolsFont()
    val scope = rememberCoroutineScope()
    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return

    LaunchedEffect(refreshTrigger) {
        val eventResult = apiClient.getEvent(eventId, currentUser.id)
        val commentsResult = apiClient.getEventComments(eventId) // Kommentare parallel laden

        isLoading = false
        if (eventResult is NetworkResult.Success) {
            event = eventResult.data
        }
        if (commentsResult is NetworkResult.Success) {
            comments = commentsResult.data
        }
    }

    Scaffold(
        topBar = { /* Deine bisherige TopAppBar bleibt identisch */
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    Box(modifier = Modifier.clickable { onNavigateBack() }.padding(8.dp)) {
                        Text(text = "arrow_back", fontFamily = iconFont, fontSize = 36.sp, color = Color.Black)
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
                // Wir nutzen jetzt verticalScroll, damit die Kommentare Platz haben!
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(e.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wann: ${e.eventTime.replace("T", " um ")} Uhr", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(e.description ?: "Keine Beschreibung hinterlegt.", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(24.dp))

                    // -- BEITRETEN BUTTON --
                    Button(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !e.isParticipating && !isJoining,
                        colors = ButtonDefaults.buttonColors(containerColor = if (e.isParticipating) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary),
                        onClick = {
                            scope.launch {
                                isJoining = true
                                val result = apiClient.joinEvent(e.id, EventActionRequest(currentUser.id))
                                isJoining = false
                                if (result is NetworkResult.Success) refreshTrigger++
                            }
                        }
                    ) {
                        if (isJoining) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        else if (e.isParticipating) Text("Du nimmst teil!")
                        else Text("Event beitreten")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // -- KOMMENTAR SEKTION --
                    Text("Kommentare (${comments.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Kommentar-Eingabe
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Schreibe einen Kommentar...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isPostingComment = true
                                    val req = CreateEventCommentRequest(e.id, currentUser.id, newCommentText.trim())
                                    val res = apiClient.postEventComment(req)
                                    isPostingComment = false
                                    if (res is NetworkResult.Success) {
                                        newCommentText = ""
                                        refreshTrigger++
                                    }
                                }
                            },
                            enabled = newCommentText.isNotBlank() && !isPostingComment,
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            if (isPostingComment) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            else Text(text = "send", fontFamily = iconFont, fontSize = 20.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Kommentar-Liste
                    if (comments.isEmpty()) {
                        Text("Noch keine Kommentare. Sei der Erste!", color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))
                    } else {
                        comments.forEach { comment ->
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                // Profilbild
                                val avatarMod = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                                if (comment.userProfilePic != null) {
                                    AsyncImage(model = comment.userProfilePic, contentDescription = null, contentScale = ContentScale.Crop, modifier = avatarMod)
                                } else {
                                    Box(modifier = avatarMod, contentAlignment = Alignment.Center) { Text("person", fontFamily = iconFont, color = Color.Gray) }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Text-Bubble
                                Column(
                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp).fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = comment.timestamp.substringAfter("T").take(5), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = comment.content, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}