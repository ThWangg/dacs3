package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.CartItem
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.OrderItem
import ltdd.dacsba.groceries.data.repository.CartRepository
import ltdd.dacsba.groceries.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ltdd.dacsba.groceries.data.constant.AppConstant
import kotlinx.coroutines.tasks.await

class BuyerCartViewModel : ViewModel() {
    private val cartRepository = CartRepository()
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    val cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val selectedItemIds = mutableStateOf<Set<String>>(emptySet())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        loadCart()
    }

    fun loadCart() {
        val userId = auth.currentUser?.uid ?: return
        isLoading.value = true
        viewModelScope.launch {
            val result = cartRepository.getCartItems(userId)
            if (result.isSuccess) {
                cartItems.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message
            }
            isLoading.value = false
        }
    }

    fun toggleSelection(productId: String) {
        val current = selectedItemIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        selectedItemIds.value = current
    }

    fun selectAll(select: Boolean) {
        if (select) {
            selectedItemIds.value = cartItems.value.map { it.productId }.toSet()
        } else {
            selectedItemIds.value = emptySet()
        }
    }

    fun removeFromCart(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = cartRepository.removeFromCart(userId, productId)
            if (result.isSuccess) {
                loadCart()
            }
        }
    }

    fun placeOrder(
        shippingAddress: String,
        note: String,
        onSuccess: (orderId: String, totalAmount: Long, sellerId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Vui lòng đăng nhập")
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                // Get buyer name
                val userDoc = db.collection(AppConstant.COLLECTION_USERS).document(userId).get().await()
                val buyerName = userDoc.getString("username") ?: "Khách hàng"

                // Filter only selected items
                val selectedItems = cartItems.value.filter { selectedItemIds.value.contains(it.productId) }
                if (selectedItems.isEmpty()) {
                    onError("Chưa có sản phẩm nào được chọn")
                    isLoading.value = false
                    return@launch
                }

                // Group items by sellerId
                val itemsBySeller = selectedItems.groupBy { it.sellerId }
                
                var successCount = 0
                var firstErrorMessage: String? = null
                for ((sellerId, items) in itemsBySeller) {
                    val orderItems = items.map {
                        OrderItem(
                            productId = it.productId,
                            productName = it.productName,
                            productImageUrl = it.productImageUrl,
                            quantity = it.quantity,
                            priceAtOrder = it.price,
                            unit = it.unit
                        )
                    }
                    val total = items.sumOf { it.price * it.quantity }

                    val order = Order(
                        buyerId = userId,
                        buyerName = buyerName,
                        sellerId = sellerId,
                        items = orderItems,
                        totalAmount = total,
                        shippingAddress = shippingAddress,
                        note = note
                    )

                    val result = orderRepository.placeOrder(order)
                    if (result.isSuccess) {
                        successCount++
                    } else {
                        if (firstErrorMessage == null) {
                            firstErrorMessage = result.exceptionOrNull()?.message
                        }
                    }
                }

                if (successCount > 0) {
                    // Tính tổng tiền cho các sản phẩm đã chọn
                    val totalAmount = selectedItems.sumOf { it.price * it.quantity }.toLong()
                    // Sinh orderId độc lập (dùng để navigate sang màn hình QR)
                    val generatedOrderId = java.util.UUID.randomUUID().toString()
                    // Lấy sellerId đầu tiên để hiển thị thông tin tài khoản
                    val firstSellerId = itemsBySeller.keys.first()

                    for (item in selectedItems) {
                        cartRepository.removeFromCart(userId, item.productId)
                    }
                    selectedItemIds.value = emptySet()
                    loadCart()
                    onSuccess(generatedOrderId, totalAmount, firstSellerId)
                } else {
                    onError(firstErrorMessage ?: "Không thể đặt hàng, vui lòng thử lại.")
                }

            } catch (e: Exception) {
                onError(e.message ?: "Đã có lỗi xảy ra")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        viewModelScope.launch {
            try {
                val productDoc = db.collection(AppConstant.COLLECTION_PRODUCTS).document(productId).get().await()
                if (productDoc.exists()) {
                    val stock = productDoc.getLong("stock")?.toInt() ?: 0
                    if (newQuantity > stock) {
                        onError("Không thể tăng thêm. Chỉ còn $stock sản phẩm trong kho.")
                        return@launch
                    }
                }
                
                val result = cartRepository.updateQuantity(userId, productId, newQuantity)
                if (result.isSuccess) {
                    loadCart()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Lỗi cập nhật số lượng")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }
}
