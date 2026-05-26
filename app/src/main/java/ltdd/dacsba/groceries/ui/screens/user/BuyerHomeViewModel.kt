package ltdd.dacsba.groceries.ui.screens.user

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.ProductRepository
import ltdd.dacsba.groceries.data.repository.SellerRequestRepository
import ltdd.dacsba.groceries.data.repository.ImageUtils
import ltdd.dacsba.groceries.data.model.CartItem
import ltdd.dacsba.groceries.data.repository.CartRepository
import ltdd.dacsba.groceries.data.repository.UserTagProfileRepository
import ltdd.dacsba.groceries.data.repository.ContentBasedFilteringEngine

class BuyerHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val productRepository = ProductRepository()
    private val sellerReqRepo = SellerRequestRepository()
    private val cartRepository = CartRepository()
    private val tagProfileRepository = UserTagProfileRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var products = mutableStateOf<List<Product>>(emptyList())
    var sellerNames = mutableStateOf<Map<String, String>>(emptyMap())
    var isLoading = mutableStateOf(false)
    var message = mutableStateOf<String?>(null)

    // ─── Recommendation ───────────────────────────────────────────────────────
    var recommendedProducts = mutableStateOf<List<Product>>(emptyList())
    var userTagProfile = mutableStateOf<List<String>>(emptyList())
    var isLoadingRecommendations = mutableStateOf(false)

    // Seller request state
    var sellerRequestStatus = mutableStateOf<String?>(null)
    var isSubmittingRequest = mutableStateOf(false)
    var requestResult = mutableStateOf<String?>(null)

    // Profile state
    var currentUser = mutableStateOf<User?>(null)
    var isUploadingAvatar = mutableStateOf(false)
    var profileMessage = mutableStateOf<String?>(null)

    init {
        fetchProducts()
        checkSellerRequestStatus()
        loadUserProfile()
        loadRecommendations()
    }

    // ─── Recommendations ──────────────────────────────────────────────────────

    fun loadRecommendations() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoadingRecommendations.value = true
            try {
                // 1. Lấy tập hợp nhãn sở thích của người dùng từ các đơn hàng đã giao (DELIVERED)
                val preferredTags = tagProfileRepository.getUserPreferredTags(uid)
                userTagProfile.value = preferredTags.toList()

                if (preferredTags.isNotEmpty()) {
                    // 2. Lấy tất cả sản phẩm và dùng Content-Based Filtering để xếp hạng độ tương đồng Cosine
                    val result = productRepository.getAllProducts()
                    result.onSuccess { allProducts ->
                        val ranked = ContentBasedFilteringEngine.rankProducts(preferredTags, allProducts)
                            .map { it.first }
                            .filter { it.stock > 0 }
                            .take(10)

                        recommendedProducts.value = ranked

                        // Load tên seller cho sản phẩm đề xuất
                        val extraSellerIds = ranked.map { it.sellerId }.distinct()
                            .filter { !sellerNames.value.containsKey(it) }
                        if (extraSellerIds.isNotEmpty()) {
                            loadSellerNamesForRecommendations(extraSellerIds)
                        }
                    }
                    result.onFailure {
                        recommendedProducts.value = emptyList()
                    }
                } else {
                    // User chưa có lịch sử mua hàng -> không có gợi ý
                    recommendedProducts.value = emptyList()
                }
            } catch (_: Exception) {
                recommendedProducts.value = emptyList()
            } finally {
                // Đảm bảo luôn reset flag dù bất kỳ điều gì xảy ra
                isLoadingRecommendations.value = false
            }
        }
    }

    private suspend fun loadSellerNamesForRecommendations(sellerIds: List<String>) {
        try {
            val nameMap = sellerNames.value.toMutableMap()
            for (sellerId in sellerIds) {
                if (sellerId.isBlank()) continue
                val doc = db.collection(AppConstant.COLLECTION_USERS)
                    .document(sellerId).get().await()
                val name = doc.getString("shopName")
                    ?: doc.getString("username")
                    ?: "Shop"
                nameMap[sellerId] = name
            }
            sellerNames.value = nameMap
        } catch (_: Exception) {}
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    fun fetchProducts() {
        viewModelScope.launch {
            isLoading.value = true
            message.value = null
            val result = productRepository.getAllProducts()
            result.onSuccess { list ->
                products.value = list
                // Load tên seller cho tất cả sản phẩm
                loadSellerNames(list.map { it.sellerId }.distinct())
            }
            result.onFailure { error -> message.value = error.message }
            isLoading.value = false
        }
    }

    private suspend fun loadSellerNames(sellerIds: List<String>) {
        if (sellerIds.isEmpty()) return
        try {
            val nameMap = mutableMapOf<String, String>()
            for (sellerId in sellerIds) {
                if (sellerId.isBlank()) continue
                val doc = db.collection(AppConstant.COLLECTION_USERS)
                    .document(sellerId).get().await()
                val name = doc.getString("shopName")
                    ?: doc.getString("username")
                    ?: "Shop"
                nameMap[sellerId] = name
            }
            sellerNames.value = nameMap
        } catch (_: Exception) {}
    }

    fun fetchProductsByCategory(categoryID: String) {
        viewModelScope.launch {
            isLoading.value = true
            message.value = null
            val result = productRepository.getProductsByCategory(categoryID)
            result.onSuccess { list -> products.value = list }
            result.onFailure { error -> message.value = error.message }
            isLoading.value = false
        }
    }

    // ─── Profile ──────────────────────────────────────────────────────────────

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val doc = db.collection(AppConstant.COLLECTION_USERS).document(uid).get().await()
                currentUser.value = doc.toObject(User::class.java)
            } catch (_: Exception) {}
        }
    }

    /**
     * Nén ảnh → Base64 → lưu thẳng vào Firestore.
     * Không cần Firebase Storage bucket.
     */
    fun uploadAndUpdateAvatar(uri: Uri) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            isUploadingAvatar.value = true
            try {
                val base64 = ImageUtils.uriToBase64(context, uri)
                db.collection(AppConstant.COLLECTION_USERS)
                    .document(uid)
                    .update("avatarUrl", base64)
                    .await()
                currentUser.value = currentUser.value?.copy(avatarUrl = base64)
                profileMessage.value = "✅ Đã cập nhật ảnh đại diện!"
            } catch (e: Exception) {
                profileMessage.value = "❌ Lỗi: ${e.message}"
            }
            isUploadingAvatar.value = false
        }
    }

    fun removeAvatar() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                db.collection(AppConstant.COLLECTION_USERS)
                    .document(uid)
                    .update("avatarUrl", "")
                    .await()
                currentUser.value = currentUser.value?.copy(avatarUrl = "")
                profileMessage.value = "Đã xóa ảnh đại diện"
            } catch (e: Exception) {
                profileMessage.value = "❌ Lỗi: ${e.message}"
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            isLoading.value = true
            try {
                db.collection(AppConstant.COLLECTION_USERS)
                    .document(uid)
                    .update("username", newUsername)
                    .await()
                currentUser.value = currentUser.value?.copy(username = newUsername)
                profileMessage.value = "✅ Đã cập nhật tên hiển thị!"
            } catch (e: Exception) {
                profileMessage.value = "❌ Lỗi: ${e.message}"
            }
            isLoading.value = false
        }
    }

    fun clearProfileMessage() { profileMessage.value = null }

    // ─── Seller Request ───────────────────────────────────────────────────────

    fun checkSellerRequestStatus() {
        viewModelScope.launch {
            sellerReqRepo.getMyRequestStatus().onSuccess { status ->
                sellerRequestStatus.value = status
            }
        }
    }

    fun submitSellerRequest(message: String) {
        viewModelScope.launch {
            isSubmittingRequest.value = true
            sellerReqRepo.submitRequest(message).onSuccess {
                sellerRequestStatus.value = "PENDING"
                requestResult.value = "✅ Yêu cầu đã được gửi! Admin sẽ xem xét sớm."
            }.onFailure { e ->
                requestResult.value = "❌ ${e.message}"
            }
            isSubmittingRequest.value = false
        }
    }

    fun clearRequestResult() { requestResult.value = null }
    
    // ─── Cart ─────────────────────────────────────────────────────────────────
    fun addToCart(product: Product, quantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Vui lòng đăng nhập")
            return
        }
        viewModelScope.launch {
            val item = CartItem(
                productId = product.id,
                productName = product.name,
                productImageUrl = product.imageUrl,
                quantity = quantity,
                price = product.price,
                unit = product.unit,
                sellerId = product.sellerId
            )
            val result = cartRepository.addToCart(uid, item)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Lỗi thêm vào giỏ hàng")
            }
        }
    }
}