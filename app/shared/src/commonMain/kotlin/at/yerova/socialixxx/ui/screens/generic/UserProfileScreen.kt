package at.yerova.socialixxx.ui.screens.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import at.yerova.socialixxx.LocalNavController
import at.yerova.socialixxx.LocalSymbolFont
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.*
import at.yerova.socialixxx.ui.ProfileAvatar
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    targetUserId: Int,
) {
    val apiClient = LocalApiClient.current
    val iconFont = LocalSymbolFont.current
    val navController = LocalNavController.current


    var userProfile by remember { mutableStateOf<UserDto?>(null) }
    var userStories by remember { mutableStateOf<List<StoryDto>>(emptyList()) }
    var userDepartments by remember { mutableStateOf<List<String>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }


    LaunchedEffect(targetUserId, refreshTrigger) {
        isLoading = true
        val userRes = apiClient.getUser(targetUserId)
        val storiesRes = apiClient.getUserStories(targetUserId)
        val spacesRes = apiClient.getSpaces(targetUserId)

        if (userRes is NetworkResult.Success) {
            userProfile = userRes.data
        } else {
            errorMessage = "Profil konnte nicht geladen werden."
        }

        if (storiesRes is NetworkResult.Success) {
            userStories = storiesRes.data
        }

        if (spacesRes is NetworkResult.Success) {
            userDepartments = spacesRes.data.filter { it.isAssigned }.map { it.name }
        }

        isLoading = false
    }

    val currentUser = LocalUser.current

    if (showCreateStoryDialog && currentUser != null) {
        CreateStoryDialog(
            userId = currentUser.id,
            apiClient = apiClient,
            onDismiss = { showCreateStoryDialog = false },
            onSuccess = {
                showCreateStoryDialog = false
                refreshTrigger++
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.displayName ?: "Profil") },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack() }) {
                        Text(text = "arrow_back", fontFamily = iconFont, fontSize = 28.sp, color = Color.Black)
                    }
                },
                actions = {
                    if (currentUser != null && targetUserId == currentUser.id) {
                        IconButton(onClick = { showCreateStoryDialog = true }) {
                            Text(text = "add_box", fontFamily = iconFont, fontSize = 28.sp, color = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else if (userProfile != null) {
            val user = userProfile!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val hasStories = userStories.isNotEmpty()

                        ProfileAvatar(
                            imageUrl = user.profilePictureUrl,
                            hasStory = hasStories,
                            size = 120.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = user.displayName, fontSize = 26.sp, fontWeight = FontWeight.Bold)

                        val depText =
                            if (userDepartments.isNotEmpty()) userDepartments.joinToString(" • ") else "Keiner Abteilung zugeordnet"
                        Text(
                            text = depText,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProfileDetailRow(icon = "badge", label = "Kurzzeichen (KZ)", value = user.kz ?: "-")
                                ProfileDetailRow(icon = "call", label = "Durchwahl (DW)", value = user.dw ?: "-")
                                ProfileDetailRow(
                                    icon = "school",
                                    label = "Lehrjahr",
                                    value = user.lehrjahr?.toString()?.let { "$it. Lehrjahr" } ?: "-")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Aktuelle Stories",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (userStories.isEmpty()) {
                        Text(
                            text = "${user.displayName} hat in den letzten 24 Stunden keine Stories gepostet.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(userStories) { story ->
                                StoryThumbnail(story = story)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileDetailRow(icon: String, label: String, value: String) {
    val iconFont = LocalSymbolFont.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = icon, fontFamily = iconFont, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StoryThumbnail(story: StoryDto) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(220.dp)
            .clickable { /* Hier könnte später ein Fullscreen-Story-Viewer aufgerufen werden */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Story",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(Color.DarkGray)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                    .padding(12.dp)
            ) {
                Text(
                    text = story.caption ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun CreateStoryDialog(
    userId: Int,
    apiClient: ApiClient,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var mediaUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neue Story posten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Teile ein besonderes Erlebnis! Füge einfach einen Link zu einem Bild ein.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Bild-URL (z.B. von Imgur)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Kleine visuelle Vorschau des Bildes, falls die URL gültig aussieht!
                if (mediaUrl.isNotBlank() && mediaUrl.startsWith("http")) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = "Vorschau",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Beschreibung (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && mediaUrl.isNotBlank(),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null

                        val req = CreateStoryRequest(
                            userId = userId,
                            mediaUrl = mediaUrl.trim(),
                            caption = caption.takeIf { it.isNotBlank() }
                        )

                        when (val res = apiClient.createStory(req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> {
                                error = res.message
                                isSubmitting = false
                            }
                        }
                    }
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Posten")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Abbrechen") }
        }
    )
}