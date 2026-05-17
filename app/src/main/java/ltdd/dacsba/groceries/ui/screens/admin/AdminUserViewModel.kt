package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.AdminRepository

class AdminUserViewModel : ViewModel() {
    private val adminRepository = AdminRepository()

    // Danh sách toàn bộ người dùng
    var allUsers = mutableStateOf<List<User>>(emptyList())
        private set

    // Danh sách sau khi lọc/tìm kiếm
    var filteredUsers = mutableStateOf<List<User>>(emptyList())
        private set

    var searchQuery = mutableStateOf("")
        private set

    // null = tất cả, "BUYER" / "SELLER" / "ADMIN"
    var selectedRoleFilter = mutableStateOf<String?>(null)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var actionMessage = mutableStateOf<String?>(null)
        private set

    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            isLoading.value = true
            val result = adminRepository.getAllUsers()
            result.onSuccess { users ->
                allUsers.value = users
                applyFilter()
            }.onFailure { error ->
                actionMessage.value = "Lỗi tải danh sách: ${error.message}"
            }
            isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        applyFilter()
    }

    fun onRoleFilterSelected(role: String?) {
        selectedRoleFilter.value = role
        applyFilter()
    }

    private fun applyFilter() {
        var result = allUsers.value

        // Lọc theo role
        val roleFilter = selectedRoleFilter.value
        if (roleFilter != null) {
            result = result.filter { it.role == roleFilter }
        }

        // Tìm kiếm theo tên hoặc email
        val query = searchQuery.value.trim()
        if (query.isNotEmpty()) {
            result = result.filter { user ->
                user.username.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
            }
        }

        filteredUsers.value = result
    }

    fun lockAccount(userId: String) {
        viewModelScope.launch {
            val result = adminRepository.toggleUserDeactivation(userId, isDeactivated = true)
            result.onSuccess {
                actionMessage.value = "Đã khóa tài khoản"
                loadAllUsers()
            }.onFailure { error ->
                actionMessage.value = "Lỗi: ${error.message}"
            }
        }
    }

    fun unlockAccount(userId: String) {
        viewModelScope.launch {
            val result = adminRepository.toggleUserDeactivation(userId, isDeactivated = false)
            result.onSuccess {
                actionMessage.value = "Đã mở khóa tài khoản"
                loadAllUsers()
            }.onFailure { error ->
                actionMessage.value = "Lỗi: ${error.message}"
            }
        }
    }

    fun clearActionMessage() {
        actionMessage.value = null
    }
}
