package at.yerova.socialixxx.ui.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
fun SpaceDetailScreen(
    spaceId: Int, spaceName: String, isAssigned: Boolean, onNavigateToPostDetail: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val iconFont = LocalSymbolFont.current
    val navController = LocalNavController.current

    var refreshFeedTrigger by remember { mutableStateOf(0) }
    var refreshQuestionsTrigger by remember { mutableStateOf(0) }
    var refreshIdeasTrigger by remember { mutableStateOf(0) }

    var showPostDialog by remember { mutableStateOf(false) }
    var showQuestionDialog by remember { mutableStateOf(false) }
    var showIdeaDialog by remember { mutableStateOf(false) }
    var showPhoneDirectory by remember { mutableStateOf(false) }

    val currentUser = LocalUser.current ?: return
    val apiClient = LocalApiClient.current

    if (showPostDialog) {
        CreateSpacePostDialog(
            spaceId = spaceId,
            userId = currentUser.id,
            apiClient = apiClient,
            onDismiss = { showPostDialog = false },
            onSuccess = { showPostDialog = false; refreshFeedTrigger++ })
    }

    if (showQuestionDialog) {
        CreateQuestionDialog(
            spaceId = spaceId,
            userId = currentUser.id,
            apiClient = apiClient,
            onDismiss = { showQuestionDialog = false },
            onSuccess = { showQuestionDialog = false; refreshQuestionsTrigger++ })
    }

    if (showIdeaDialog) {
        CreateIdeaDialog(
            spaceId = spaceId,
            apiClient = apiClient,
            onDismiss = { showIdeaDialog = false },
            onSuccess = { showIdeaDialog = false; refreshIdeasTrigger++ })
    }

    if (showPhoneDirectory) {
        PhoneDirectoryDialog(
            spaceId = spaceId, spaceName = spaceName, apiClient = apiClient, onDismiss = { showPhoneDirectory = false })
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(spaceName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Text(
                                text = "arrow_back",
                                fontFamily = iconFont,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showPhoneDirectory = true }) {
                            Text(
                                text = "contact_phone",
                                fontFamily = iconFont,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Updates", fontWeight = FontWeight.Medium) })
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Fragenkatalog", fontWeight = FontWeight.Medium) })
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Ideenbox", fontWeight = FontWeight.Medium) })
                }
            }
        },
        floatingActionButton = {
            if (isAssigned || selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> showPostDialog = true
                            1 -> showQuestionDialog = true
                            2 -> showIdeaDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "add",
                        fontFamily = iconFont,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> SpaceFeedTab(spaceId, refreshFeedTrigger, { refreshFeedTrigger++ }, onNavigateToPostDetail)
                1 -> SpaceQuestionsTab(spaceId, refreshQuestionsTrigger, { refreshQuestionsTrigger++ })
                2 -> SpaceIdeasTab(spaceId, refreshIdeasTrigger, { refreshIdeasTrigger++ })
            }
        }
    }
}

@Composable
fun SpaceFeedTab(spaceId: Int, refreshTrigger: Int, onRefresh: () -> Unit, onNavigateToPostDetail: (Int) -> Unit) {
    val apiClient = LocalApiClient.current
    var posts by remember { mutableStateOf<List<SpacePostDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(spaceId, refreshTrigger) {
        isLoading = true
        val result = apiClient.getSpacePosts(spaceId)
        isLoading = false
        isRefreshing = false
        if (result is NetworkResult.Success) {
            posts = result.data
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading && !isRefreshing && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Text("Noch keine Updates. Teile einen Tipp!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(posts) { post ->
                    SpacePostItem(post = post, onClick = { onNavigateToPostDetail(post.id) })
                }
            }
        }
    }
}

@Composable
fun SpacePostItem(post: SpacePostDto, onClick: () -> Unit) {
    val iconFont = LocalSymbolFont.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val avatarMod = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)

                if (post.authorProfilePic != null) {
                    AsyncImage(
                        model = post.authorProfilePic,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = avatarMod
                    )
                } else {
                    Box(modifier = avatarMod, contentAlignment = Alignment.Center) {
                        Text("person", fontFamily = iconFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.timestamp.substringAfter("T").take(5),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.content,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                maxLines = 4,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!post.mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Post Bild",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "chat_bubble",
                    fontFamily = iconFont,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${post.commentCount} Kommentare",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SpaceQuestionsTab(spaceId: Int, refreshTrigger: Int, onRefresh: () -> Unit) {
    val apiClient = LocalApiClient.current
    val iconFont = LocalSymbolFont.current
    var questions by remember { mutableStateOf<List<QuestionDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(spaceId, refreshTrigger) {
        isLoading = true
        val result = apiClient.getSpaceQuestions(spaceId)
        isLoading = false
        isRefreshing = false
        if (result is NetworkResult.Success) {
            questions = result.data
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading && !isRefreshing && questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                Text("Keine Fragen vorhanden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(questions) { question ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "help",
                                    fontFamily = iconFont,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = question.questionTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (expanded) "expand_less" else "expand_more",
                                    fontFamily = iconFont,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = question.answerText,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Hinzugefügt von ${question.authorName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpaceIdeasTab(spaceId: Int, refreshTrigger: Int, onRefresh: () -> Unit) {
    val apiClient = LocalApiClient.current
    val iconFont = LocalSymbolFont.current
    var ideas by remember { mutableStateOf<List<IdeaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(spaceId, refreshTrigger) {
        isLoading = true
        val result = apiClient.getSpaceIdeas(spaceId)
        isLoading = false
        isRefreshing = false
        if (result is NetworkResult.Success) {
            ideas = result.data
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading && !isRefreshing && ideas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (ideas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                Text("Die Ideenbox ist leer. Wirf eine Idee ein!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ideas) { idea ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFFF9C4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "lightbulb",
                                        fontFamily = iconFont,
                                        color = Color(0xFFFBC02D),
                                        fontSize = 24.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Anonyme Idee",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = idea.timestamp.substringAfter("T").take(5),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = idea.content,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateSpacePostDialog(
    spaceId: Int, userId: Int, apiClient: ApiClient, onDismiss: () -> Unit, onSuccess: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Neues Update teilen", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val focusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Was gibt es Neues?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Bild-URL (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null
                        val req = CreateSpacePostRequest(
                            spaceId, userId, content.trim(), mediaUrl.trim().takeIf { it.isNotBlank() })
                        when (val res = apiClient.createSpacePost(spaceId, req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> {
                                error = res.message; isSubmitting = false
                            }
                        }
                    }
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Posten", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun CreateQuestionDialog(
    spaceId: Int, userId: Int, apiClient: ApiClient, onDismiss: () -> Unit, onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Fragenkatalog erweitern", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Frage / Problem") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Antwort / Anleitung") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && title.isNotBlank() && answer.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null
                        val req = CreateQuestionRequest(spaceId, userId, title.trim(), answer.trim())
                        when (val res = apiClient.createSpaceQuestion(spaceId, req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> {
                                error = res.message; isSubmitting = false
                            }
                        }
                    }
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Speichern", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun CreateIdeaDialog(
    spaceId: Int, apiClient: ApiClient, onDismiss: () -> Unit, onSuccess: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Idee einreichen", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Deine Idee wird komplett anonym eingereicht. Kein Name wird gespeichert.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Beschreibe deine Idee...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null
                        val req = CreateIdeaRequest(spaceId, content.trim())
                        when (val res = apiClient.createSpaceIdea(spaceId, req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> {
                                error = res.message; isSubmitting = false
                            }
                        }
                    }
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Absenden", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun PhoneDirectoryDialog(
    spaceId: Int, spaceName: String, apiClient: ApiClient, onDismiss: () -> Unit
) {
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val uriHandler = LocalUriHandler.current
    val iconFont = LocalSymbolFont.current

    LaunchedEffect(spaceId) {
        val res = apiClient.getUsers(departmentId = spaceId)
        if (res is NetworkResult.Success) {
            users = res.data.sortedBy { it.displayName }
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Team $spaceName", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (users.isEmpty()) {
                Text("Keine Mitglieder gefunden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(users) { user ->
                        val hasPhone = !user.dw.isNullOrBlank()
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = hasPhone) {
                                if (hasPhone) {
                                    uriHandler.openUri("tel:${user.dw}")
                                }
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProfileAvatar(
                                    imageUrl = user.profilePictureUrl, size = 40.dp, hasStory = user.hasActiveStory
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasPhone) {
                                        Text(
                                            "DW: ${user.dw}",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    } else {
                                        Text(
                                            "Keine Durchwahl",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            if (hasPhone) {
                                Text(
                                    "call",
                                    fontFamily = iconFont,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}