package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.OrderStatus
import ltdd.dacsba.groceries.data.repository.OrderRepository

class BuyerOrderViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val orders = mutableStateOf<List<Order>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)

    init {
        loadOrders()
    }

    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        isLoading.value = true
        viewModelScope.launch {
            val result = orderRepository.getBuyerOrders(userId)
            if (result.isSuccess) {
                orders.value = result.getOrNull() ?: emptyList()
                errorMessage.value = null
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Không thể tải đơn hàng"
            }
            isLoading.value = false
        }
    }

    fun cancelOrder(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.cancelOrderAndRestoreStock(orderId)
            if (result.isSuccess) {
                loadOrders()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Không thể hủy đơn hàng")
            }
        }
    }
}
