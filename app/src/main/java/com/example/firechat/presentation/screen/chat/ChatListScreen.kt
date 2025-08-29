package com.example.firechat.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.firechat.data.model.Chat
import com.example.firechat.data.model.MessageType
import com.example.firechat.data.model.User
import com.example.firechat.presentation.viewmodel.AuthViewModel
import com.example.firechat.presentation.viewmodel.ChatListViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (String, User) -> Unit,
    onStartChatClick: () -> Unit,
    onLogout: () -> Unit,
    chatListViewModel: ChatListViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by chatListViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle(User())
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text("Chats", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                actions = {
                    // Current user avatar
                    currentUser?.let { user ->
                        UserAvatar(
                            user = user,
                            size = 36.dp,
                            onClick = onLogout
                        )
                    }
                }
            )
            
            // Chat List
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.isEmpty) {
                    EmptyChatList(onStartChatClick = onStartChatClick)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.chats) { chat ->
                            val otherUser = chat.getOtherParticipant(currentUser?.id ?: "")
                            if (otherUser != null) {
                                ChatListItem(
                                    chat = chat,
                                    otherUser = otherUser,
                                    onClick = { onChatClick(chat.id, otherUser) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom Navigation
            BottomNavigation()
        }
        
        // Floating Action Button
        if (!uiState.isEmpty) {
            FloatingActionButton(
                onClick = onStartChatClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Start new chat")
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    otherUser: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar with online indicator
        Box {
            UserAvatar(
                user = otherUser,
                size = 50.dp
            )
            
            if (otherUser.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Chat Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherUser.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Time
                Text(
                    text = formatTime(chat.lastMessageTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Last message with sender prefix
                Text(
                    text = formatLastMessage(chat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Unread count badge
                if (chat.unreadCount > 0) {
                    Badge(
                        modifier = Modifier.size(20.dp)
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserAvatar(
    user: User,
    size: Dp,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .let { if (onClick != null) it.clickable { onClick() } else it }
    
    if (user.avatarUrl.isNotEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "${user.displayName} avatar",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.initials,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyChatList(
    onStartChatClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Conversations Yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start a new chat or invite others to join the conversation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartChatClick,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("Start New Chat")
            }
        }
    }
}

@Composable
fun BottomNavigation() {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationButton(
                icon = { Icon(Icons.Default.Person, contentDescription = "Chats") },
                label = "Chats",
                selected = true
            )
            NavigationButton(
                icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                label = "Calls",
                selected = false
            )
            NavigationButton(
                icon = { Icon(Icons.Default.Person, contentDescription = "Users") },
                label = "Users",
                selected = false
            )
            NavigationButton(
                icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                label = "Groups",
                selected = false
            )
        }
    }
}

@Composable
fun NavigationButton(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { }
            .padding(8.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        else -> {
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

private fun formatLastMessage(chat: Chat): String {
    val prefix = if (chat.lastMessageSender.isNotEmpty()) {
        when (chat.lastMessageType) {
            MessageType.TEXT, MessageType.EMOJI -> "Sender: "
            else -> ""
        }
    } else ""
    
    return prefix + chat.getDisplayLastMessage()
}