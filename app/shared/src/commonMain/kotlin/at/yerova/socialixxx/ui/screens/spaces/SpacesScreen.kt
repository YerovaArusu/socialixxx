package at.yerova.socialixxx.ui.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.api.SpaceDto
import at.yerova.socialixxx.ui.screens.NavigationBar
import at.yerova.socialixxx.ui.screens.NavigationTopBar

@Composable
fun SpacesScreen(
    onNavigateToSpaceDetail: (Int, String, Boolean) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToWorkplace: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit
) {
    val apiClient = LocalApiClient.current
    val currentUser = LocalUser.current ?: return

    var spaces by remember { mutableStateOf<List<SpaceDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = apiClient.getSpaces(currentUser.id)
        isLoading = false
        if (result is NetworkResult.Success) {
            spaces = result.data
        }
    }

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = "Spaces",
                onAddClick = { /* Später für Admin-Zwecke */ },
                onProfileClick = { onNavigateToUserProfile(currentUser.id) }
            )
        },
        bottomBar = {
            NavigationBar(2, onNavigateToEvents, onNavigateToChats, {}, onNavigateToWorkplace)
        },
        containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (spaces.isEmpty()) {
                Text("Keine Spaces verfügbar.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(spaces) { space ->
                        SpaceCard(
                            space = space,
                            onClick = { onNavigateToSpaceDetail(space.id, space.name, space.isAssigned) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpaceCard(space: SpaceDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Macht die Kachel quadratisch
            .clickable { onClick() }
            .let {
                if (space.isAssigned) it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else it.border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (space.isAssigned) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFFF0F0F0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = space.kz,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (space.isAssigned) MaterialTheme.colorScheme.primary else Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = space.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (space.isAssigned) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Zugeordnet", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}