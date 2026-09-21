package at.yerova.socialixxx.ui.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.compose.rememberNavController
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalSymbolFont
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.CreateSpacePostCommentRequest
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.api.SpacePostCommentDto
import at.yerova.socialixxx.api.SpacePostDto
import at.yerova.socialixxx.ui.ProfileAvatar
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun SpacePostDetailScreen(
    spaceId: Int,
    postId: Int,
    isAssigned: Boolean,
) {
    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return
    val iconFont = LocalSymbolFont.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var post by remember { mutableStateOf<SpacePostDto?>(null) }
    var comments by remember { mutableStateOf<List<SpacePostCommentDto>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var isPostingComment by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(spaceId, postId) {
        val postsRes = apiClient.getSpacePosts(spaceId)
        val commentsRes = apiClient.getSpacePostComments(postId)

        if (postsRes is NetworkResult.Success) {
            post = postsRes.data.find { it.id == postId }
        }
        if (commentsRes is NetworkResult.Success) {
            comments = commentsRes.data
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack() }) {
                        Text(text = "arrow_back", fontFamily = iconFont, fontSize = 28.sp, color = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (isAssigned) {
                Surface(shadowElevation = 16.dp, color = Color.White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Kommentar schreiben...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isPostingComment = true
                                    val req =
                                        CreateSpacePostCommentRequest(postId, currentUser.id, newCommentText.trim())
                                    val res = apiClient.createSpacePostComment(postId, req)
                                    if (res is NetworkResult.Success) {
                                        comments = comments + res.data
                                        newCommentText = ""
                                    }
                                    isPostingComment = false
                                }
                            },
                            enabled = newCommentText.isNotBlank() && !isPostingComment,
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(48.dp)
                        ) {
                            if (isPostingComment) CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                            else Text(text = "send", fontFamily = iconFont, fontSize = 24.sp, color = Color.White)
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
            } else if (post == null) {
                Text("Post nicht gefunden.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    ProfileAvatar(
                                        imageUrl = post!!.authorProfilePic,
                                        size = 40.dp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = post!!.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = post!!.timestamp.substringAfter("T").take(5),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = post!!.content, fontSize = 16.sp, lineHeight = 22.sp)

                                // Das Content-Bild (Media URL) behält weiterhin AsyncImage
                                if (!post!!.mediaUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    AsyncImage(
                                        model = post!!.mediaUrl,
                                        contentDescription = "Post Bild",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 400.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Kommentare (${comments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (comments.isEmpty()) {
                        item { Text("Noch keine Kommentare.", color = Color.Gray) }
                    } else {
                        items(comments) { comment ->
                            Row(modifier = Modifier.fillMaxWidth()) {

                                ProfileAvatar(
                                    imageUrl = comment.authorProfilePic,
                                    size = 32.dp
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                        .padding(12.dp).fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(
                                            text = comment.timestamp.substringAfter("T").take(5),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
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