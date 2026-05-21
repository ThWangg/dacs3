package ltdd.dacsba.groceries.data.model

data class ChatRoom(
    val roomId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCounts: Map<String, Int> = emptyMap() // Map<UserId, UnreadCount>
)
