package at.yerova.socialixxx.ui.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalNavController
import at.yerova.socialixxx.LocalSymbolFont
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.*
import at.yerova.socialixxx.ui.NavigationBar
import at.yerova.socialixxx.ui.NavigationTopBar
import at.yerova.socialixxx.ui.ProfileAvatar
import at.yerova.socialixxx.ui.screens.ChatDetailScreenRoute
import at.yerova.socialixxx.ui.screens.UserProfileScreenRoute
import at.yerova.socialixxx.ui.screens.generic.UserProfileScreen
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen() {
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
                onAddClick = { showNewChatDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(currentTab = 1)
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
                            Text("Keine aktiven Chats.", color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        }
                    } else {
                        items(activeChats) { chat ->
                            ChatListItem(chat = chat, apiClient = apiClient)
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
                                onAccept = { refreshTrigger++ },
                                onDecline = { refreshTrigger++ }
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
    apiClient: ApiClient
) {
    val navController = LocalNavController.current // Universal-Access
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
            .clickable { navController.navigate(ChatDetailScreenRoute(chat.id)) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            imageUrl = partnerPicUrl,
            hasStory = hasStory,
            size = 54.dp,
            onClick = { navController.navigate(UserProfileScreenRoute(chat.partnerId)) }
        )

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
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val navController = LocalNavController.current
    val iconFont = LocalSymbolFont.current
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
        ProfileAvatar(
            imageUrl = partnerPicUrl,
            hasStory = hasStory,
            size = 54.dp,
            onClick = { navController.navigate(UserProfileScreenRoute(chat.partnerId)) }
        )

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
    val iconFont = LocalSymbolFont.current

    LaunchedEffect(Unit) {
        val res = apiClient.getUsers()
        isLoading = false
        if (res is NetworkResult.Success) users = res.data
    }

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
                                ProfileAvatar(
                                    imageUrl = user.profilePictureUrl,
                                    hasStory = user.hasActiveStory,
                                    size = 40.dp
                                    // onClick lassen wir hier absichtlich null, damit der Klick auf die ganze Zeile greift
                                )
                                Spacer(modifier = Modifier.width(12.dp))
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