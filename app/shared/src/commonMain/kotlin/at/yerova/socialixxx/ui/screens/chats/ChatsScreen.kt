package at.yerova.socialixxx.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.*
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import at.yerova.socialixxx.ui.screens.NavigationBar
import at.yerova.socialixxx.ui.screens.NavigationTopBar
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen(
    onNavigateToChatDetail: (Int, String, Int) -> Unit,
    onNavigateToUserProfile: (Int) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToWorkplace: () -> Unit
) {
    var chats by remember { mutableStateOf<List<ChatDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var showNewChatDialog by remember { mutableStateOf(false) }


    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        val result = apiClient.getChats(currentUser.id)
        isLoading = false
        if (result is NetworkResult.Success) {
            chats = result.data
        }
    }

    val activeChats = chats.filter { it.status == 1 }
    val chatRequests = chats.filter { it.status == 0 }

    if (showNewChatDialog) {
        NewChatDialog(
            currentUserId = currentUser.id,
            existingChats = chats,
            apiClient = apiClient,
            onDismiss = { showNewChatDialog = false },
            onChatCreated = {
                showNewChatDialog = false
                refreshTrigger++
            }
        )
    }

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = "Nachrichten",
                onAddClick = { showNewChatDialog = true },
                onProfileClick = { onNavigateToUserProfile(currentUser.id) }
            )
        },
        bottomBar = {
            NavigationBar(1, onNavigateToEvents, {}, onNavigateToTeam, onNavigateToWorkplace)
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && chats.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Aktive Chats",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    if (activeChats.isEmpty()) {
                        item {
                            Text(
                                "Keine aktiven Chats.",
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    } else {
                        items(activeChats) { chat ->
                            ChatListItem(
                                chat = chat,
                                apiClient = apiClient,
                                onClick = { onNavigateToChatDetail(chat.id, chat.partnerName, chat.partnerId) },
                                onProfileClick = { onNavigateToUserProfile(chat.partnerId) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        Text(
                            text = "Chatanfragen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    if (chatRequests.isEmpty()) {
                        item { Text("Keine ausstehenden Anfragen.", color = Color.Gray) }
                    } else {
                        items(chatRequests) { request ->
                            ChatRequestItem(
                                chat = request,
                                apiClient = apiClient,
                                onProfileClick = { onNavigateToUserProfile(request.partnerId) },
                                onAccept = {
                                    refreshTrigger++
                                },
                                onDecline = {
                                    refreshTrigger++
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: ChatDto,
    apiClient: ApiClient,
    onClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val iconFont = getMaterialSymbolsFont()
    var partnerPicUrl by remember { mutableStateOf<String?>(null) }
    var hasStory by remember { mutableStateOf(false) }

    LaunchedEffect(chat.partnerId) {
        val res = apiClient.getUser(chat.partnerId)
        if (res is NetworkResult.Success) {
            partnerPicUrl = res.data.profilePictureUrl
            hasStory = res.data.hasActiveStory
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val storyBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFfeda75),
                Color(0xFFfa7e1e),
                Color(0xFFd62976),
                Color(0xFF962fbf),
                Color(0xFF4f5bd5)
            )
        )

        // HIER IST DER FIX: Harte Größe, runder Zuschnitt, DANN klickbar!
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
                .let {
                    if (hasStory) {
                        it.border(2.5.dp, storyBrush, CircleShape).padding(4.dp)
                    } else it
                }
        ) {
            if (partnerPicUrl != null) {
                AsyncImage(
                    model = partnerPicUrl,
                    contentDescription = "Profil",
                    contentScale = ContentScale.Crop,
                    // fillMaxSize nimmt jetzt den perfekt berechneten Platz ein
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "person", fontFamily = iconFont, fontSize = 36.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = chat.partnerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = chat.lastMessage ?: "Noch keine Nachrichten.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChatRequestItem(
    chat: ChatDto,
    apiClient: ApiClient,
    onProfileClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val iconFont = getMaterialSymbolsFont()
    val scope = rememberCoroutineScope()

    var partnerPicUrl by remember { mutableStateOf<String?>(null) }
    var hasStory by remember { mutableStateOf(false) }

    LaunchedEffect(chat.partnerId) {
        val res = apiClient.getUser(chat.partnerId)
        if (res is NetworkResult.Success) {
            partnerPicUrl = res.data.profilePictureUrl
            hasStory = res.data.hasActiveStory
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val storyBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFfeda75),
                Color(0xFFfa7e1e),
                Color(0xFFd62976),
                Color(0xFF962fbf),
                Color(0xFF4f5bd5)
            )
        )

        // HIER EBENFALLS DER FIX
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
                .let {
                    if (hasStory) {
                        it.border(2.5.dp, storyBrush, CircleShape).padding(4.dp)
                    } else it
                }
        ) {
            if (partnerPicUrl != null) {
                AsyncImage(
                    model = partnerPicUrl,
                    contentDescription = "Profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "person", fontFamily = iconFont, fontSize = 36.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(text = chat.partnerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))

        IconButton(onClick = {
            scope.launch {
                apiClient.updateChatStatus(chat.id, UpdateChatStatusRequest(chat.partnerId, 1))
                onAccept()
            }
        }) {
            Text(text = "check_circle", fontFamily = iconFont, fontSize = 28.sp, color = Color(0xFF4CAF50))
        }
        IconButton(onClick = {
            scope.launch {
                apiClient.updateChatStatus(chat.id, UpdateChatStatusRequest(chat.partnerId, 4))
                onDecline()
            }
        }) {
            Text(text = "cancel", fontFamily = iconFont, fontSize = 28.sp, color = Color(0xFFF44336))
        }
    }
}

@Composable
fun NewChatDialog(
    currentUserId: Int,
    existingChats: List<ChatDto>,
    apiClient: ApiClient,
    onDismiss: () -> Unit,
    onChatCreated: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val res = apiClient.getUsers()
        isLoading = false
        if (res is NetworkResult.Success) users = res.data
    }
    val iconFont = getMaterialSymbolsFont()

    val existingPartnerIds = existingChats.map { it.partnerId }
    val filteredUsers = users.filter {
        it.id != currentUserId &&
                !existingPartnerIds.contains(it.id) &&
                it.displayName.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neuen Chat starten") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Suchen...") },
                    leadingIcon = {
                        Text(text = "search", fontFamily = iconFont, fontSize = 50.sp, color = Color.Gray)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (filteredUsers.isEmpty()) {
                    Text("Keine neuen Personen gefunden.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredUsers) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            apiClient.createChat(CreateChatRequest(currentUserId, user.id))
                                            onChatCreated()
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user.displayName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}