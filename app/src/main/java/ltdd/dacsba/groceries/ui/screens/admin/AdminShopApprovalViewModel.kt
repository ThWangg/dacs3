package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.AdminRepository

class AdminShopApprovalViewModel : ViewModel() {
    private val adminRepository = AdminRepository()

    // Danh sách các tài khoản seller đang chờ duyệt
    var pendingSellerList = mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var actionMessage = mutableStateOf<String?>(null)
        private set

    init {
        loadPendingRequests()
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            isLoading.value = true
            val result = adminRepository.getPendingSellerRequests()
            result.onSuccess { users ->
                pendingSellerList.value = users
            }.onFailure { error ->
                actionMessage.value = "Lỗi tải danh sách: ${error.message}"
            }
            isLoading.value = false
        }
    }

    fun approveShop(userId: String) {
        viewModelScope.launch {
            val result = adminRepository.approveSellerRequest(userId)
            result.onSuccess {
                actionMessage.value = "Đã duyệt gian hàng thành công"
                loadPendingRequests()
            }.onFailure { error ->
                actionMessage.value = "Lỗi duyệt: ${error.message}"
            }
        }
    }

    fun rejectShop(userId: String) {
        viewModelScope.launch {
            val result = adminRepository.rejectSellerRequest(userId)
            result.onSuccess {
                actionMessage.value = "Đã từ chối yêu cầu"
                loadPendingRequests()
            }.onFailure { error ->
                actionMessage.value = "Lỗi từ chối: ${error.message}"
            }
        }
    }

    fun clearActionMessage() {
        actionMessage.value = null
    }
}
