package ltdd.dacsba.groceries.data.model

data class Message(
    val messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class Conversation(
    val conversationId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCountForSeller: Int = 0
)
