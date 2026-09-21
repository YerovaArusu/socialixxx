package at.yerova.socialixxx.ui.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.*
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun SpaceDetailScreen(
    spaceId: Int,
    spaceName: String,
    isAssigned: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val iconFont = getMaterialSymbolsFont()

    var refreshFeedTrigger by remember { mutableStateOf(0) }
    var refreshQuestionsTrigger by remember { mutableStateOf(0) }

    var showPostDialog by remember { mutableStateOf(false) }
    var showQuestionDialog by remember { mutableStateOf(false) }

    val currentUser = LocalUser.current ?: return
    val apiClient = LocalApiClient.current

    if (showPostDialog) {
        CreateSpacePostDialog(
            spaceId = spaceId,
            userId = currentUser.id,
            apiClient = apiClient,
            onDismiss = { showPostDialog = false },
            onSuccess = {
                showPostDialog = false
                refreshFeedTrigger++
            }
        )
    }

    if (showQuestionDialog) {
        CreateQuestionDialog(
            spaceId = spaceId,
            userId = currentUser.id,
            apiClient = apiClient,
            onDismiss = { showQuestionDialog = false },
            onSuccess = {
                showQuestionDialog = false
                refreshQuestionsTrigger++
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(spaceName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Text(text = "arrow_back", fontFamily = iconFont, fontSize = 28.sp, color = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Updates", fontWeight = FontWeight.Medium) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Fragenkatalog", fontWeight = FontWeight.Medium) })
                }
            }
        },
        floatingActionButton = {
            if (isAssigned) {
                FloatingActionButton(
                    onClick = { if (selectedTab == 0) showPostDialog = true else showQuestionDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = "add", fontFamily = iconFont, fontSize = 24.sp, color = Color.White)
                }
            }
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (selectedTab == 0) {
                // NEU: Wir geben die Navigation an den Feed-Tab weiter
                SpaceFeedTab(spaceId, refreshFeedTrigger, onNavigateToPostDetail)
            } else {
                SpaceQuestionsTab(spaceId, refreshQuestionsTrigger)
            }
        }
    }
}

@Composable
fun SpaceFeedTab(spaceId: Int, refreshTrigger: Int, onNavigateToPostDetail: (Int) -> Unit) {
    val apiClient = LocalApiClient.current
    var posts by remember { mutableStateOf<List<SpacePostDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(spaceId, refreshTrigger) {
        isLoading = true
        val result = apiClient.getSpacePosts(spaceId)
        isLoading = false
        if (result is NetworkResult.Success) {
            posts = result.data
        }
    }

    if (isLoading && posts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (posts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Noch keine Updates. Teile einen Tipp!", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(posts) { post ->
                SpacePostItem(post = post, onClick = { onNavigateToPostDetail(post.id) })
            }
        }
    }
}
@Composable
fun SpacePostItem(post: SpacePostDto, onClick: () -> Unit) {
    val iconFont = getMaterialSymbolsFont()

    // Die gesamte Karte ist jetzt klickbar und navigiert zum neuen Screen
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val avatarMod = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                if (post.authorProfilePic != null) {
                    AsyncImage(model = post.authorProfilePic, contentDescription = null, contentScale = ContentScale.Crop, modifier = avatarMod)
                } else {
                    Box(modifier = avatarMod, contentAlignment = Alignment.Center) { Text("person", fontFamily = iconFont, color = Color.Gray) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = post.timestamp.substringAfter("T").take(5), fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text auf 4 Zeilen begrenzen (ist ja nur die Vorschau)
            Text(text = post.content, fontSize = 15.sp, lineHeight = 20.sp, maxLines = 4)

            if (!post.mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Post Bild",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "chat_bubble", fontFamily = iconFont, fontSize = 18.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${post.commentCount} Kommentare", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SpaceQuestionsTab(spaceId: Int, refreshTrigger: Int) {
    val apiClient = LocalApiClient.current
    var questions by remember { mutableStateOf<List<QuestionDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(spaceId, refreshTrigger) {
        isLoading = true
        val result = apiClient.getSpaceQuestions(spaceId)
        isLoading = false
        if (result is NetworkResult.Success) {
            questions = result.data
        }
    }

    if (isLoading && questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Keine Fragen vorhanden.", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(questions) { question ->
                // Fragenkatalog mit Akkordeon-Aufklappfunktion (Antwort mit Anleitung)[cite: 5]
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "help", fontFamily = getMaterialSymbolsFont(), color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = question.questionTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text(text = if (expanded) "expand_less" else "expand_more", fontFamily = getMaterialSymbolsFont(), fontSize = 24.sp, color = Color.Gray)
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color(0xFFF0F0F0))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = question.answerText, fontSize = 15.sp, lineHeight = 22.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Hinzugefügt von ${question.authorName}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// --- DIALOGE ---

@Composable
fun CreateSpacePostDialog(
    spaceId: Int,
    userId: Int,
    apiClient: ApiClient,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neues Update teilen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    label = { Text("Was gibt es Neues?") },
                    modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), // NEU!
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) } // NEU: Tab navigiert jetzt runter!
                    )
                )
                OutlinedTextField(
                    value = mediaUrl, onValueChange = { mediaUrl = it },
                    label = { Text("Bild-URL (Optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && content.isNotBlank(),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null
                        val req = CreateSpacePostRequest(spaceId, userId, content.trim(), mediaUrl.trim().takeIf { it.isNotBlank() })
                        when (val res = apiClient.createSpacePost(spaceId, req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> { error = res.message; isSubmitting = false }
                        }
                    }
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Posten")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Abbrechen") } }
    )
}

@Composable
fun CreateQuestionDialog(
    spaceId: Int,
    userId: Int,
    apiClient: ApiClient,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fragenkatalog erweitern") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Frage / Problem") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = answer, onValueChange = { answer = it },
                    label = { Text("Antwort / Anleitung") },
                    modifier = Modifier.fillMaxWidth(), minLines = 4
                )
                if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && title.isNotBlank() && answer.isNotBlank(),
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        error = null
                        val req = CreateQuestionRequest(spaceId, userId, title.trim(), answer.trim())
                        when (val res = apiClient.createSpaceQuestion(spaceId, req)) {
                            is NetworkResult.Success -> onSuccess()
                            is NetworkResult.Error -> { error = res.message; isSubmitting = false }
                        }
                    }
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Speichern")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Abbrechen") } }
    )
}