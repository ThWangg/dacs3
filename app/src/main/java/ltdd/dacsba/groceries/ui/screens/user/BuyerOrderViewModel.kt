package ltdd.dacsba.groceries.ui.screens.user

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.OrderStatus
import ltdd.dacsba.groceries.data.model.Review
import ltdd.dacsba.groceries.data.repository.ImageUtils
import ltdd.dacsba.groceries.data.repository.OrderRepository
import ltdd.dacsba.groceries.data.repository.ReviewRepository

class BuyerOrderViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val orderRepository = OrderRepository()
    private val reviewRepository = ReviewRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val orders = mutableStateOf<List<Order>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)

    /** Set các key "orderId_productId" đã được review — dùng để disable nút đánh giá */
    val reviewedKeys = mutableStateOf<Set<String>>(emptySet())

    /** Trạng thái đang gửi review */
    val isSubmittingReview = mutableStateOf(false)
    val reviewSubmitMessage = mutableStateOf<String?>(null)

    init {
        loadOrders()
    }

    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        isLoading.value = true
        viewModelScope.launch {
            val result = orderRepository.getBuyerOrders(userId)
            if (result.isSuccess) {
                val loaded = result.getOrNull() ?: emptyList()
                orders.value = loaded
                errorMessage.value = null
                // Sau khi load, kiểm tra các sản phẩm đã review
                loadReviewedKeys(userId, loaded)
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Không thể tải đơn hàng"
            }
            isLoading.value = false
        }
    }

    private suspend fun loadReviewedKeys(userId: String, loadedOrders: List<Order>) {
        val deliveredOrderIds = loadedOrders
            .filter { it.status == OrderStatus.DELIVERED }
            .map { it.orderId }
            .filter { it.isNotBlank() }
        if (deliveredOrderIds.isEmpty()) return
        val keys = reviewRepository.getReviewedKeys(userId, deliveredOrderIds)
        reviewedKeys.value = keys
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

    /**
     * Gửi đánh giá sản phẩm. imageUri là tùy chọn.
     */
    fun submitReview(
        orderId: String,
        productId: String,
        rating: Double,
        comment: String,
        imageUri: Uri?,
        productName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("Vui lòng đăng nhập"); return }
        viewModelScope.launch {
            isSubmittingReview.value = true
            try {
                // Lấy tên người dùng
                val buyerName = try {
                    val doc = db.collection(AppConstant.COLLECTION_USERS).document(uid).get().await()
                    doc.getString("username") ?: "Khách hàng"
                } catch (_: Exception) { "Khách hàng" }

                // Convert ảnh feedback sang base64 nếu có
                val imageBase64 = if (imageUri != null) {
                    try {
                        withContext(Dispatchers.IO) {
                            ImageUtils.uriToBase64(context, imageUri)
                        }
                    } catch (_: Exception) { "" }
                } else ""

                val review = Review(
                    orderId = orderId,
                    productId = productId,
                    buyerId = uid,
                    buyerName = buyerName,
                    rating = rating,
                    comment = comment.trim(),
                    imageUrl = imageBase64,
                    createdAt = System.currentTimeMillis()
                )

                val result = reviewRepository.submitReview(review)
                if (result.isSuccess) {
                    // Cập nhật reviewedKeys cục bộ ngay để disable nút
                    reviewedKeys.value = reviewedKeys.value + "${orderId}_${productId}"
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Không thể gửi đánh giá")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi không xác định")
            } finally {
                isSubmittingReview.value = false
            }
        }
    }
}
