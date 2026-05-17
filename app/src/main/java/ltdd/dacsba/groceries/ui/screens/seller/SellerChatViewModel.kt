package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.Conversation
import ltdd.dacsba.groceries.data.model.Message
import ltdd.dacsba.groceries.data.repository.ChatRepository

class SellerChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    // Danh sách tất cả cuộc hội thoại của seller
    var conversations = mutableStateOf<List<Conversation>>(emptyList())
        private set

    // Cuộc hội thoại đang được chọn để xem chi tiết
    var selectedConversation = mutableStateOf<Conversation?>(null)
        private set

    // Danh sách tin nhắn trong cuộc hội thoại đang chọn
    var messages = mutableStateOf<List<Message>>(emptyList())
        private set

    // Nội dung đang nhập trong ô chat
    var messageInputText = mutableStateOf("")
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    // Giữ tham chiếu listener để hủy khi không cần nữa
    private var messageListenerRegistration: ListenerRegistration? = null

    init {
        loadConversations()
    }

    fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading.value = true
            val result = chatRepository.getConversationsForSeller(currentUserId)
            result.onSuccess { list ->
                conversations.value = list
            }.onFailure { error ->
                errorMessage.value = "Lỗi tải hội thoại: ${error.message}"
            }
            isLoading.value = false
        }
    }

    // Chọn một cuộc hội thoại và bắt đầu lắng nghe tin nhắn real-time
    fun selectConversation(conversation: Conversation) {
        selectedConversation.value = conversation
        startListeningToMessages(conversation.conversationId)
    }

    // Quay lại danh sách hội thoại
    fun clearSelectedConversation() {
        selectedConversation.value = null
        messages.value = emptyList()
        stopListeningToMessages()
    }

    private fun startListeningToMessages(conversationId: String) {
        stopListeningToMessages()
        messageListenerRegistration = chatRepository.listenToMessages(conversationId) { updatedMessages ->
            messages.value = updatedMessages
        }
    }

    private fun stopListeningToMessages() {
        messageListenerRegistration?.remove()
        messageListenerRegistration = null
    }

    fun onMessageInputChange(text: String) {
        messageInputText.value = text
    }

    fun sendMessage() {
        val currentUserId = auth.currentUser?.uid ?: return
        val conversation = selectedConversation.value ?: return
        val content = messageInputText.value.trim()
        if (content.isEmpty()) return

        val newMessage = Message(
            conversationId = conversation.conversationId,
            senderId = currentUserId,
            receiverId = conversation.buyerId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val result = chatRepository.sendMessage(newMessage)
            if (result.isSuccess) {
                messageInputText.value = ""
            } else {
                errorMessage.value = "Gửi tin nhắn thất bại"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningToMessages()
    }
}
