package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.model.ChatRoom
import ltdd.dacsba.groceries.data.model.Message
import ltdd.dacsba.groceries.data.model.User
import java.util.UUID

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatRoomsRef = db.collection("chat_rooms")
    private val usersRef = db.collection("users")

    suspend fun getOrCreateChatRoom(currentUserId: String, otherUserId: String): Result<String> {
        return try {
            if (currentUserId.isEmpty() || otherUserId.isEmpty()) {
                throw Exception("Invalid user IDs")
            }

val query1 = chatRoomsRef
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()

            for (document in query1.documents) {
                val room = document.toObject(ChatRoom::class.java)
                if (room != null && room.participants.contains(otherUserId)) {
                    return Result.success(room.roomId)
                }
            }

val newRoomId = UUID.randomUUID().toString()
            val newRoom = ChatRoom(
                roomId = newRoomId,
                participants = listOf(currentUserId, otherUserId),
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                unreadCounts = mapOf(currentUserId to 0, otherUserId to 0)
            )

            chatRoomsRef.document(newRoomId).set(newRoom).await()
            Result.success(newRoomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChatRooms(userId: String): Flow<List<ChatRoom>> = callbackFlow {
        val listener = chatRoomsRef
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoom::class.java)
                } ?: emptyList()
                
                trySend(rooms).isSuccess
            }

        awaitClose { listener.remove() }
    }

    fun getMessages(roomId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatRoomsRef.document(roomId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                trySend(messages).isSuccess
            }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(roomId: String, senderId: String, content: String): Result<Boolean> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val message = Message(
                messageId = messageId,
                senderId = senderId,
                content = content,
                timestamp = System.currentTimeMillis()
            )

            db.runTransaction { transaction ->
                val roomRef = chatRoomsRef.document(roomId)
                val roomSnapshot = transaction.get(roomRef)
                
                if (roomSnapshot.exists()) {
                    val room = roomSnapshot.toObject(ChatRoom::class.java)
                    if (room != null) {

                        val receiverId = room.participants.find { it != senderId } ?: ""
                        val unreadCounts = room.unreadCounts.toMutableMap()
                        if (receiverId.isNotEmpty()) {
                            unreadCounts[receiverId] = (unreadCounts[receiverId] ?: 0) + 1
                        }

                        transaction.update(
                            roomRef, mapOf(
                                "lastMessage" to content,
                                "lastMessageTime" to message.timestamp,
                                "unreadCounts" to unreadCounts
                            )
                        )
                    }
                }

                val msgRef = roomRef.collection("messages").document(messageId)
                transaction.set(msgRef, message)
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetUnreadCount(roomId: String, userId: String): Result<Boolean> {
        return try {
            db.runTransaction { transaction ->
                val roomRef = chatRoomsRef.document(roomId)
                val roomSnapshot = transaction.get(roomRef)
                
                if (roomSnapshot.exists()) {
                    val room = roomSnapshot.toObject(ChatRoom::class.java)
                    if (room != null) {
                        val unreadCounts = room.unreadCounts.toMutableMap()
                        unreadCounts[userId] = 0
                        transaction.update(roomRef, "unreadCounts", unreadCounts)
                    }
                }
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(userId: String): Result<User> {
        return try {
            val snapshot = usersRef.document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminUser(): User? {
        return try {
            val snapshot = usersRef
                .whereEqualTo("role", "ADMIN")
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
