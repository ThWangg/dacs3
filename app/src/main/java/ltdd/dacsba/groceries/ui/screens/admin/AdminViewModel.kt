package ltdd.dacsba.groceries.ui.screens.admin

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.model.SellerRequest
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.AdminRepository
import ltdd.dacsba.groceries.data.repository.StorageRepository

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val repo = AdminRepository()
    private val storageRepo = StorageRepository()

    val isLoading = mutableStateOf(false)
    val isUploading = mutableStateOf(false)
    val users = mutableStateOf<List<User>>(emptyList())
    val products = mutableStateOf<List<Product>>(emptyList())
    val pendingRequests = mutableStateOf<List<SellerRequest>>(emptyList())
    val snackMessage = mutableStateOf<String?>(null)

    // Profile
    val adminEmail = mutableStateOf(repo.getCurrentAdminEmail())
    val adminUsername = mutableStateOf("Admin")
    val adminAvatarUrl = mutableStateOf("")   // avatar của admin hiện tại
    val isEditingProfile = mutableStateOf(false)
    val editUsername = mutableStateOf("Admin")

    // Stats
    val totalUsers get() = users.value.size
    val totalProducts get() = products.value.size
    val totalBuyers get() = users.value.count { it.role == "BUYER" }
    val totalSellers get() = users.value.count { it.role == "SELLER" }

    init { loadAll(); loadAdminProfile() }

    fun loadAdminProfile() {
        viewModelScope.launch {
            try {
                val uid = repo.getCurrentAdminUid()
                if (uid.isBlank()) return@launch
                val doc = FirebaseFirestore.getInstance()
                    .collection(AppConstant.COLLECTION_USERS).document(uid)
                    .get().await()
                doc.getString("avatarUrl")?.let { adminAvatarUrl.value = it }
                doc.getString("username")?.let {
                    if (it.isNotBlank()) { adminUsername.value = it; editUsername.value = it }
                }
            } catch (_: Exception) {}
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            isLoading.value = true
            launch { repo.getAllUsers().onSuccess { users.value = it } }
            launch { repo.getAllProducts().onSuccess { products.value = it } }
            isLoading.value = false
        }
    }

    // ─── Image Upload ─────────────────────────────────────────────────────────

    /**
     * Upload ảnh từ Gallery lên Firebase Storage.
     * @param uri Uri của ảnh được chọn từ Gallery
     * @param folder Thư mục lưu trong Storage ("products" hoặc "avatars")
     * @param onComplete Callback trả về download URL sau khi upload xong
     */
    fun uploadImage(uri: Uri, folder: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            isUploading.value = true
            val fileName = "${folder}/${System.currentTimeMillis()}.jpg"
            try {
                // Đọc bytes trước — đáng tin cậy hơn putFile() với content:// URI
                val bytes = context.contentResolver.openInputStream(uri)
                    ?.readBytes()
                    ?: throw Exception("Không đọc được file ảnh")
                storageRepo.uploadImageBytes(bytes, fileName).onSuccess { url ->
                    onComplete(url)
                }.onFailure {
                    snackMessage.value = "Upload ảnh thất bại: ${it.message}"
                }
            } catch (e: Exception) {
                snackMessage.value = "Lỗi: ${e.message}"
            }
            isUploading.value = false
        }
    }

    /** Upload avatar cho chính admin đang đăng nhập — dùng putBytes để tránh lỗi content:// */
    fun uploadAdminAvatar(uri: Uri) {
        viewModelScope.launch {
            isUploading.value = true
            val uid = repo.getCurrentAdminUid()
            if (uid.isBlank()) {
                snackMessage.value = "Lỗi: không tìm thấy UID"
                isUploading.value = false
                return@launch
            }
            try {
                val bytes = context.contentResolver.openInputStream(uri)
                    ?.readBytes()
                    ?: throw Exception("Không đọc được file ảnh")
                val fileName = "avatars/${uid}_${System.currentTimeMillis()}.jpg"
                storageRepo.uploadImageBytes(bytes, fileName).onSuccess { downloadUrl ->
                    repo.updateUserAvatar(uid, downloadUrl).onSuccess {
                        adminAvatarUrl.value = downloadUrl
                        snackMessage.value = "✅ Đã cập nhật ảnh đại diện!"
                    }.onFailure { snackMessage.value = "Lưu thất bại: ${it.message}" }
                }.onFailure { snackMessage.value = "Upload thất bại: ${it.message}" }
            } catch (e: Exception) {
                snackMessage.value = "Lỗi đọc ảnh: ${e.message}"
            }
            isUploading.value = false
        }
    }

    /** Xóa avatar của admin */
    fun removeAdminAvatar() {
        viewModelScope.launch {
            val uid = repo.getCurrentAdminUid()
            if (uid.isBlank()) return@launch
            repo.updateUserAvatar(uid, "").onSuccess {
                adminAvatarUrl.value = ""
                snackMessage.value = "Đã xóa ảnh đại diện"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
        }
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    fun toggleUserDeactivate(user: User) {
        viewModelScope.launch {
            val newState = !user.isDeactivated
            repo.toggleUserDeactivate(user.uid, newState).onSuccess {
                users.value = users.value.map {
                    if (it.uid == user.uid) it.copy(isDeactivated = newState) else it
                }
                snackMessage.value = if (newState) "Đã khóa ${user.username}" else "Đã mở khóa ${user.username}"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
        }
    }

    fun updateUserAvatar(uid: String, avatarUrl: String) {
        viewModelScope.launch {
            repo.updateUserAvatar(uid, avatarUrl).onSuccess {
                users.value = users.value.map {
                    if (it.uid == uid) it.copy(avatarUrl = avatarUrl) else it
                }
                snackMessage.value = if (avatarUrl.isBlank()) "Đã xóa ảnh đại diện" else "Đã cập nhật ảnh đại diện ✅"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
        }
    }

    // ─── Seller Requests ────────────────────────────────────────────────────────

    fun loadPendingRequests() {
        viewModelScope.launch {
            isLoading.value = true
            repo.getPendingRequests().onSuccess {
                pendingRequests.value = it
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
            isLoading.value = false
        }
    }

    fun approveSellerRequest(request: SellerRequest) {
        viewModelScope.launch {
            repo.approveSellerRequest(request).onSuccess {
                pendingRequests.value = pendingRequests.value.filter { it.requestId != request.requestId }
                // Cập nhật role trong danh sách users
                users.value = users.value.map {
                    if (it.uid == request.uid) it.copy(role = "SELLER") else it
                }
                snackMessage.value = "✅ Đã duyệt ${request.username} lên Seller!"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
        }
    }

    fun rejectSellerRequest(requestId: String) {
        viewModelScope.launch {
            repo.rejectSellerRequest(requestId).onSuccess {
                val name = pendingRequests.value.find { it.requestId == requestId }?.username ?: ""
                pendingRequests.value = pendingRequests.value.filter { it.requestId != requestId }
                snackMessage.value = "Yêu cầu của $name đã bị từ chối."
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
        }
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    fun addProduct(product: Product) {
        viewModelScope.launch {
            isLoading.value = true
            repo.addProduct(product).onSuccess { newId ->
                products.value = listOf(product.copy(id = newId)) + products.value
                snackMessage.value = "Đã thêm \"${product.name}\" ✅"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
            isLoading.value = false
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            isLoading.value = true
            repo.updateProduct(product).onSuccess {
                products.value = products.value.map { if (it.id == product.id) product else it }
                snackMessage.value = "Đã cập nhật \"${product.name}\" ✅"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
            isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            isLoading.value = true
            val name = products.value.find { it.id == productId }?.name ?: ""
            // Xóa ảnh trên Storage nếu là ảnh từ Firebase
            val imageUrl = products.value.find { it.id == productId }?.imageUrl ?: ""
            if (imageUrl.contains("firebasestorage")) {
                storageRepo.deleteImageByUrl(imageUrl)
            }
            repo.deleteProduct(productId).onSuccess {
                products.value = products.value.filter { it.id != productId }
                snackMessage.value = "Đã xóa \"$name\""
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
            isLoading.value = false
        }
    }

    fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            isLoading.value = true
            val count = repo.getProductCount()
            if (count == 0) {
                repo.seedSampleProducts().onSuccess { added ->
                    snackMessage.value = "Đã thêm $added sản phẩm mẫu vào Firestore! ✅"
                    repo.getAllProducts().onSuccess { products.value = it }
                }.onFailure { snackMessage.value = "Lỗi seed: ${it.message}" }
            } else {
                snackMessage.value = "Đã có $count sản phẩm trong Firestore"
            }
            isLoading.value = false
        }
    }

    // ─── Profile ──────────────────────────────────────────────────────────────

    fun enterEditProfile() { editUsername.value = adminUsername.value; isEditingProfile.value = true }
    fun cancelEditProfile() { isEditingProfile.value = false }

    fun saveProfile() {
        viewModelScope.launch {
            isLoading.value = true
            repo.updateAdminProfile(repo.getCurrentAdminUid(), editUsername.value).onSuccess {
                adminUsername.value = editUsername.value
                snackMessage.value = "Đã cập nhật thông tin!"
            }.onFailure { snackMessage.value = "Lỗi: ${it.message}" }
            isEditingProfile.value = false
            isLoading.value = false
        }
    }

    fun clearSnack() { snackMessage.value = null }
    fun logout() = repo.logout()
}
