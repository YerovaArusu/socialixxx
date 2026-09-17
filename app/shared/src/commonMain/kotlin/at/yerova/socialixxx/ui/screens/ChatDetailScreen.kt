package at.yerova.socialixxx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import at.yerova.socialixxx.api.*
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(
    chatId: Int,
    onNavigateBack: () -> Unit
) {
    var chat by remember { mutableStateOf<ChatDto?>(null) }

    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var partnerPicUrl by remember { mutableStateOf<String?>(null) }
    var partnerName by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val iconFont = getMaterialSymbolsFont()
    val scope = rememberCoroutineScope()

    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return

    LaunchedEffect(chatId, currentUser) {
        val result = apiClient.getChat(chatId, currentUser.id)
        isLoading = false
        if (result !is NetworkResult.Success) {
            println("Error while getting chat with id $chatId")
            return@LaunchedEffect
        }
        chat = result.data
        if (chat == null) {
            return@LaunchedEffect
        }

        val userResult = apiClient.getUser(chat!!.partnerId)

        if (userResult !is NetworkResult.Success) {
            println("Error while getting user with id ${chat!!.partnerId}")
            return@LaunchedEffect
        }

        partnerPicUrl = userResult.data.profilePictureUrl
        partnerName = userResult.data.displayName
    }

    val listState = rememberLazyListState()

    LaunchedEffect(refreshTrigger) {
        val res = apiClient.getMessages(chatId, currentUser.id)
        isLoading = false
        if (res is NetworkResult.Success) {
            messages = res.data
            apiClient.markMessagesAsRead(chatId, MarkReadRequest(currentUser.id))
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Text(text = "arrow_back", fontFamily = iconFont, fontSize = 28.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    if (partnerPicUrl != null) {
                        AsyncImage(
                            model = partnerPicUrl,
                            contentDescription = "Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                        )
                    } else {
                        Text(text = "account_circle", fontFamily = iconFont, fontSize = 40.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    partnerName?.let { Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nachricht schreiben...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable(enabled = inputText.isNotBlank() && !isSending) {
                                scope.launch {
                                    isSending = true
                                    val textToSend = inputText.trim()
                                    inputText = ""
                                    val res =
                                        apiClient.sendMessage(chatId, SendMessageRequest(currentUser.id, textToSend))
                                    isSending = false
                                    if (res is NetworkResult.Success) {
                                        refreshTrigger++
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "send", fontFamily = iconFont, fontSize = 24.sp, color = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (messages.isEmpty()) {
                Text(
                    "Noch keine Nachrichten. Schreib das erste 'Hallo'!",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == currentUser.id
                        MessageBubble(message = msg, isMe = isMe)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageDto, isMe: Boolean) {
    val time = message.timestamp.substringAfter("T").take(5)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isMe) MaterialTheme.colorScheme.primary else Color.White,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .border(if (isMe) 0.dp else 1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = if (isMe) Color.White else Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = if (isMe) Color(0xFFE0E0E0) else Color.Gray,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}