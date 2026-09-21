package at.yerova.socialixxx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalNavController
import at.yerova.socialixxx.LocalSymbolFont
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.ui.screens.*
import coil3.compose.AsyncImage

@Composable
fun ProfileAvatar(
    imageUrl: String?,
    hasStory: Boolean = false,
    size: Dp = 54.dp,
    onClick: (() -> Unit)? = null
) {
    val iconFont = LocalSymbolFont.current
    val storyBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFfeda75),
            Color(0xFFfa7e1e),
            Color(0xFFd62976),
            Color(0xFF962fbf),
            Color(0xFF4f5bd5)
        )
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .let { if (hasStory) it.border(2.5.dp, storyBrush, CircleShape).padding(4.dp) else it }
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "person",
                    fontFamily = iconFont,
                    fontSize = (size.value * 0.6).sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NavigationBar(currentTab: Int) {
    val iconFont = LocalSymbolFont.current
    val navController = LocalNavController.current

    fun navigateBottomTab(route: Any) {
        navController.navigate(route) {
            popUpTo<EventsScreenRoute> { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        TabItem("calendar_today", currentTab == 0, { navigateBottomTab(EventsScreenRoute) }, iconFont)
        TabItem("chat", currentTab == 1, { navigateBottomTab(ChatsScreenRoute) }, iconFont)
        TabItem("group", currentTab == 2, { navigateBottomTab(SpacesScreenRoute) }, iconFont)
        TabItem("build", currentTab == 3, { navigateBottomTab(WorkplaceScreenRoute) }, iconFont)
    }
}

@Composable
fun RowScope.TabItem(
    iconName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconFont: FontFamily
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = iconName, fontFamily = iconFont, fontSize = 26.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                )
            }
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = Color.LightGray
        )
    )
}

@Composable
fun NavigationTopBar(
    title: String,
    onAddClick: () -> Unit
) {
    val iconFont = LocalSymbolFont.current
    val currentUser = LocalUser.current ?: return
    val navController = LocalNavController.current

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                        .clickable { onAddClick() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "add", fontFamily = iconFont, fontSize = 28.sp, color = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Normal)
            }

            ProfileAvatar(
                imageUrl = currentUser.profilePictureUrl,
                hasStory = currentUser.hasActiveStory,
                size = 40.dp,
                onClick = { navController.navigate(UserProfileScreenRoute(currentUser.id)) }
            )
        }
    }
}