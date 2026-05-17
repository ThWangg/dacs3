package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ltdd.dacsba.groceries.data.model.Conversation
import ltdd.dacsba.groceries.data.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ===================== Screen (kết nối ViewModel) =====================

@Composable
fun SellerChatScreen(
    viewModel: SellerChatViewModel = viewModel()
) {
    val conversations by viewModel.conversations
    val selectedConversation by viewModel.selectedConversation
    val messages by viewModel.messages
    val messageInputText by viewModel.messageInputText
    val isLoading by viewModel.isLoading

    if (selectedConversation == null) {
        // Màn hình danh sách hội thoại
        ConversationListContent(
            conversations = conversations,
            isLoading = isLoading,
            onConversationClick = { conversation ->
                viewModel.selectConversation(conversation)
            },
            onRefresh = { viewModel.loadConversations() }
        )
    } else {
        // Màn hình chi tiết chat
        ChatDetailContent(
            conversation = selectedConversation!!,
            messages = messages,
            messageInputText = messageInputText,
            onBackClick = { viewModel.clearSelectedConversation() },
            onMessageInputChange = { viewModel.onMessageInputChange(it) },
            onSendClick = { viewModel.sendMessage() }
        )
    }
}

// ===================== UI: Danh sách hội thoại =====================

@Composable
fun ConversationListContent(
    conversations: List<Conversation>,
    isLoading: Boolean,
    onConversationClick: (Conversation) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7CB342))
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Tin nhắn khách hàng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF7CB342)
            )
        }

        if (conversations.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có tin nhắn nào",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation) }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar chữ cái đầu tên buyer
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color(0xFFE8F5E9)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = conversation.buyerName.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7CB342)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = conversation.buyerName.ifBlank { "Khách hàng" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (conversation.lastMessageTime > 0L) {
                    Text(
                        text = formatMessageTime(conversation.lastMessageTime),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = conversation.lastMessage.ifBlank { "Bắt đầu cuộc trò chuyện..." },
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Badge số tin nhắn chưa đọc
        if (conversation.unreadCountForSeller > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color(0xFF7CB342)
            ) {
                Text(
                    text = conversation.unreadCountForSeller.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ===================== UI: Chi tiết hội thoại =====================

@Composable
fun ChatDetailContent(
    conversation: Conversation,
    messages: List<Message>,
    messageInputText: String,
    onBackClick: () -> Unit,
    onMessageInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    val listState = rememberLazyListState()

    // Tự động cuộn xuống tin nhắn mới nhất
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .imePadding()
    ) {
        // Header với tên buyer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7CB342))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = conversation.buyerName.firstOrNull()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = conversation.buyerName.ifBlank { "Khách hàng" },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        // Danh sách tin nhắn
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isSellerMessage = message.senderId != conversation.buyerId
                MessageBubble(
                    message = message,
                    isFromSeller = isSellerMessage
                )
            }
        }

        // Ô nhập tin nhắn
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInputText,
                onValueChange = onMessageInputChange,
                placeholder = { Text("Nhập tin nhắn...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7CB342),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = if (messageInputText.isNotBlank()) Color(0xFF7CB342) else Color(0xFFE0E0E0),
                modifier = Modifier
                    .size(44.dp)
                    .clickable(enabled = messageInputText.isNotBlank()) { onSendClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromSeller: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromSeller) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromSeller) 16.dp else 4.dp,
                bottomEnd = if (isFromSeller) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromSeller) Color(0xFF7CB342) else Color.White
            ),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = if (isFromSeller) Color.White else Color.DarkGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatMessageTime(message.timestamp),
                    fontSize = 10.sp,
                    color = if (isFromSeller) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// ===================== Preview =====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConversationListPreview() {
    val mockConversations = listOf(
        Conversation(
            conversationId = "buyer1_seller1",
            buyerId = "buyer1",
            buyerName = "Nguyễn Văn A",
            lastMessage = "Táo còn hàng không shop?",
            lastMessageTime = System.currentTimeMillis() - 300000,
            unreadCountForSeller = 2
        ),
        Conversation(
            conversationId = "buyer2_seller1",
            buyerId = "buyer2",
            buyerName = "Trần Thị B",
            lastMessage = "Shop ơi cho mình hỏi giá",
            lastMessageTime = System.currentTimeMillis() - 3600000,
            unreadCountForSeller = 0
        )
    )
    ConversationListContent(
        conversations = mockConversations,
        isLoading = false,
        onConversationClick = {},
        onRefresh = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatDetailPreview() {
    val mockConversation = Conversation(
        conversationId = "buyer1_seller1",
        buyerName = "Nguyễn Văn A",
        buyerId = "buyer1"
    )
    val mockMessages = listOf(
        Message(senderId = "buyer1", receiverId = "seller1", content = "Táo còn hàng không shop?"),
        Message(senderId = "seller1", receiverId = "buyer1", content = "Dạ còn bạn ơi, còn khoảng 50kg"),
        Message(senderId = "buyer1", receiverId = "seller1", content = "Cho mình 2kg nhé")
    )
    ChatDetailContent(
        conversation = mockConversation,
        messages = mockMessages,
        messageInputText = "",
        onBackClick = {},
        onMessageInputChange = {},
        onSendClick = {}
    )
}
