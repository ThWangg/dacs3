package ltdd.dacsba.groceries.ui.screens.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.ChatRoom
import ltdd.dacsba.groceries.data.model.Message
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.ChatRepository

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser: StateFlow<User?> = _otherUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _adminUserId = MutableStateFlow<String?>(null)
    val adminUserId: StateFlow<String?> = _adminUserId

    private var rawRooms = emptyList<ChatRoom>()

    init {
        if (currentUserId.isNotEmpty()) {
            loadAdminUserId()
            loadChatRooms()
        }
    }

    private fun loadAdminUserId() {
        viewModelScope.launch {
            val adminUser = chatRepository.getAdminUser()
            _adminUserId.value = adminUser?.uid
            updateSortedRooms()

adminUser?.uid?.let { adminUid ->
                if (currentUserId != adminUid) {
                    chatRepository.getOrCreateChatRoom(currentUserId, adminUid)
                }
            }
        }
    }

    private fun loadChatRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getChatRooms(currentUserId).collect { rooms ->
                rawRooms = rooms
                updateSortedRooms()
                _isLoading.value = false
            }
        }
    }

    private fun updateSortedRooms() {
        val adminUid = _adminUserId.value
        val sortedRooms = if (adminUid != null && currentUserId != adminUid) {
            rawRooms.sortedWith { r1, r2 ->
                val r1IsAdmin = r1.participants.contains(adminUid)
                val r2IsAdmin = r2.participants.contains(adminUid)
                when {
                    r1IsAdmin && !r2IsAdmin -> -1
                    !r1IsAdmin && r2IsAdmin -> 1
                    else -> r2.lastMessageTime.compareTo(r1.lastMessageTime)
                }
            }
        } else {
            rawRooms
        }
        _chatRooms.value = sortedRooms
    }

    fun loadMessages(roomId: String, otherUserId: String) {
        viewModelScope.launch {

            val result = chatRepository.getUserInfo(otherUserId)
            result.onSuccess { user ->
                _otherUser.value = user
            }.onFailure {
                _error.value = "Không thể tải thông tin người dùng"
            }

chatRepository.getMessages(roomId).collect { msgList ->
                _messages.value = msgList

                chatRepository.resetUnreadCount(roomId, currentUserId)
            }
        }
    }

    fun sendMessage(roomId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val result = chatRepository.sendMessage(roomId, currentUserId, content)
            result.onFailure {
                _error.value = it.message
            }
        }
    }

    fun getOrCreateChatRoom(targetUserId: String, onComplete: (String?) -> Unit) {
        if (currentUserId.isEmpty()) {
            _error.value = "Bạn cần đăng nhập để sử dụng tính năng chat"
            onComplete(null)
            return
        }
        if (currentUserId == targetUserId) {
            _error.value = "Bạn không thể chat với chính mình"
            onComplete(null)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRepository.getOrCreateChatRoom(currentUserId, targetUserId)
            result.onSuccess { roomId ->
                onComplete(roomId)
            }.onFailure {
                _error.value = it.message
                onComplete(null)
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
