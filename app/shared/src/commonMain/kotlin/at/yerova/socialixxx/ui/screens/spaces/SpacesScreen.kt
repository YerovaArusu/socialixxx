package at.yerova.socialixxx.ui.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.LocalSymbolFont
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.api.SpaceDto
import at.yerova.socialixxx.ui.NavigationBar
import at.yerova.socialixxx.ui.NavigationTopBar

@Composable
fun SpacesScreen(
    onNavigateToSpaceDetail: (Int, String, Boolean) -> Unit,
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
            title = "Spaces", onAddClick = { /* Später für Admin-Zwecke */ })
    }, bottomBar = {
        NavigationBar(2)
    }, containerColor = Color(0xFFF5F3F7)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (spaces.isEmpty()) {
                Text("Keine Spaces verfügbar.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(spaces) { space ->
                        SpaceCard(
                            space = space,
                            onClick = { onNavigateToSpaceDetail(space.id, space.name, space.isAssigned) })
                    }
                }
            }
        }
    }
}

@Composable
fun SpaceCard(space: SpaceDto, onClick: () -> Unit) {
    val iconFont = LocalSymbolFont.current

    val borderColor = if (space.isAssigned) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    val borderWidth = if (space.isAssigned) 2.dp else 1.dp

    Card(
        modifier = Modifier.fillMaxWidth().border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.Start) {
                Text(text = "Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = space.kz,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (space.isAssigned) MaterialTheme.colorScheme.primary else Color.Black
                )
            }

            Box(modifier = Modifier.width(1.dp).height(80.dp).background(Color.Black))
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = space.name.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = space.description ?: "Keine Beschreibung.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color.DarkGray,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (space.isAssigned) {
                            Text(
                                text = "edit_square",
                                fontFamily = iconFont,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zugeordnet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = "visibility", fontFamily = iconFont, fontSize = 16.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Read-Only", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}