package ltdd.dacsba.groceries.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import ltdd.dacsba.groceries.data.model.ChatRoom
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.ChatRepository
import androidx.compose.ui.layout.ContentScale
import ltdd.dacsba.groceries.ui.components.SmartImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    chatViewModel: ChatViewModel = viewModel()
) {
    val chatRooms by chatViewModel.chatRooms.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tin nhắn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        if (isLoading && chatRooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (chatRooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có cuộc trò chuyện nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(chatRooms) { room ->
                    ChatRoomItem(
                        room = room,
                        currentUserId = currentUserId,
                        onClick = {
                            val otherId = room.participants.find { it != currentUserId } ?: return@ChatRoomItem
                            navController.navigate("chat_detail/${room.roomId}/$otherId")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    room: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
) {
    val chatRepo = remember { ChatRepository() }
    var otherUser by remember { mutableStateOf<User?>(null) }
    val otherUserId = room.participants.find { it != currentUserId } ?: ""
    val unreadCount = room.unreadCounts[currentUserId] ?: 0

    LaunchedEffect(otherUserId) {
        if (otherUserId.isNotEmpty()) {
            chatRepo.getUserInfo(otherUserId).onSuccess { user ->
                otherUser = user
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = otherUser?.avatarUrl
            if (!avatarUrl.isNullOrBlank()) {
                SmartImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = otherUser?.username ?: "Đang tải...",
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (otherUser?.role == "ADMIN") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        color = Color(0xFF1976D2).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Admin",
                            color = Color(0xFF1976D2),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = room.lastMessage.ifEmpty { "Chưa có tin nhắn" },
                color = if (unreadCount > 0) Color.Black else Color.Gray,
                fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTime(room.lastMessageTime),
                color = Color.Gray,
                fontSize = 12.sp
            )
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    if (timeMillis == 0L) return ""
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timeMillis }

    return if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(msgTime.time)
    } else {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(msgTime.time)
    }
}
