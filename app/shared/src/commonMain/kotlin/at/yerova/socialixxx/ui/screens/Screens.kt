package at.yerova.socialixxx.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.yerova.socialixxx.LocalUser
import at.yerova.socialixxx.ui.getMaterialSymbolsFont
import coil3.compose.AsyncImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("login")
object LoginScreen

@Serializable
@SerialName("register")
object RegisterScreen

@Serializable
@SerialName("chats")
object ChatsScreen

@Serializable
@SerialName("chat")
data class ChatDetailRoute(
    val chatId: Int
)

@Serializable
@SerialName("user")
data class UserProfileRoute(val targetUserId: Int)

@Serializable
@SerialName("teams")
object TeamScreen

@Serializable
@SerialName("workplace")
object WorkplaceScreen

@Serializable
@SerialName("events")
object EventsScreen

@Serializable
@SerialName("event_details")
data class EventDetailRoute(val eventId: Int)

@Composable
fun NavigationBar(
    currentTab: Int,
    onNavigateToEvents: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToWorkplace: () -> Unit
) {
    val iconFont = getMaterialSymbolsFont()

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        TabItem(
            iconName = "calendar_today",
            isSelected = currentTab == 0,
            onClick = onNavigateToEvents,
            iconFont = iconFont
        )
        TabItem(iconName = "chat", isSelected = currentTab == 1, onClick = onNavigateToChats, iconFont = iconFont)
        TabItem(iconName = "group", isSelected = currentTab == 2, onClick = onNavigateToTeam, iconFont = iconFont)
        TabItem(iconName = "build", isSelected = currentTab == 3, onClick = onNavigateToWorkplace, iconFont = iconFont)
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
                Text(
                    text = iconName,
                    fontFamily = iconFont,
                    fontSize = 26.sp
                )

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
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val iconFont = getMaterialSymbolsFont()
    val currentUser = LocalUser.current ?: return

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

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(40.dp)
            ) {
                if (currentUser.profilePictureUrl != null) {
                    AsyncImage(
                        model = currentUser.profilePictureUrl,
                        contentDescription = "Profilbild",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                } else {
                    Text(
                        text = "account_circle",
                        fontFamily = iconFont,
                        fontSize = 36.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}