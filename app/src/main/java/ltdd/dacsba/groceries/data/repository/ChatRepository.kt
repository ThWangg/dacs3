package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Conversation
import ltdd.dacsba.groceries.data.model.Message

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val conversationCollection = db.collection(AppConstant.COLLECTION_CONVERSATIONS)
    private val messageCollection = db.collection(AppConstant.COLLECTION_MESSAGES)

    // Lấy danh sách cuộc hội thoại của một seller
    suspend fun getConversationsForSeller(sellerId: String): Result<List<Conversation>> {
        return try {
            val snapshot = conversationCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .await()
            val conversations = snapshot.toObjects(Conversation::class.java)
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy toàn bộ tin nhắn trong một cuộc hội thoại (một lần)
    suspend fun getMessages(conversationId: String): Result<List<Message>> {
        return try {
            val snapshot = messageCollection
                .whereEqualTo("conversationId", conversationId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            val messages = snapshot.toObjects(Message::class.java)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lắng nghe tin nhắn real-time (trả về ListenerRegistration để có thể hủy)
    fun listenToMessages(
        conversationId: String,
        onMessagesUpdated: (List<Message>) -> Unit
    ): ListenerRegistration {
        return messageCollection
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                onMessagesUpdated(messages)
            }
    }

    // Gửi tin nhắn và cập nhật thông tin cuộc hội thoại
    suspend fun sendMessage(message: Message): Result<Boolean> {
        return try {
            val documentReference = messageCollection.document()
            val messageWithId = message.copy(messageId = documentReference.id)
            documentReference.set(messageWithId).await()

            // Cập nhật tin nhắn cuối cùng trong cuộc hội thoại
            val conversationUpdate = mapOf(
                "lastMessage" to message.content,
                "lastMessageTime" to message.timestamp
            )
            conversationCollection
                .document(message.conversationId)
                .update(conversationUpdate)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
