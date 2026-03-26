package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant


class SellerProfileViewModel : ViewModel() {

    data class SellerProfileUiState(
        val username: String = "",
        val email: String = "",
        val shopName: String = "",
        val phone: String = "",
        val updateMessage: String? = null
    )
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var uiState = mutableStateOf(SellerProfileUiState())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var isEditMode = mutableStateOf(false)
        private set

    //lưu data gốc để restore khi bỏ edit
    private var originalUsername = ""
    private var originalShopName = ""
    private var originalPhone = ""

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            isLoading.value = true
            try {
                val document = db.collection(AppConstant.COLLECTION_USERS)
                    .document(currentUser.uid)
                    .get()
                    .await()

                val username = document.getString("username") ?: ""
                val shopName = document.getString("shopName") ?: ""
                val phone = document.getString("phone") ?: ""
                val email = currentUser.email ?: ""

                originalUsername = username
                originalShopName = shopName
                originalPhone = phone

                uiState.value = uiState.value.copy(
                    username = username,
                    email = email,
                    shopName = shopName,
                    phone = phone
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    updateMessage = "Lỗi tải thông tin: ${e.message}"
                )
            }
            isLoading.value = false
        }
    }

    //lưu lại tên trước khi vô chỉnh sửa
    fun enterEditMode() {
        originalUsername = uiState.value.username
        originalShopName = uiState.value.shopName
        originalPhone = uiState.value.phone
        isEditMode.value = true
    }

    //restore lại nếu huỷ edit
    fun cancelEdit() {
        uiState.value = uiState.value.copy(
            username = originalUsername,
            shopName = originalShopName,
            phone = originalPhone
        )
        isEditMode.value = false
    }

    fun onUsernameChange(value: String) {
        uiState.value = uiState.value.copy(username = value)
    }

    fun onShopNameChange(value: String) {
        uiState.value = uiState.value.copy(shopName = value)
    }

    fun onPhoneChange(value: String) {
        uiState.value = uiState.value.copy(phone = value)
    }

    fun saveProfile() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            isLoading.value = true
            try {
                val updates = mapOf(
                    "username" to uiState.value.username,
                    "shopName" to uiState.value.shopName,
                    "phone" to uiState.value.phone
                )
                db.collection(AppConstant.COLLECTION_USERS)
                    .document(currentUser.uid)
                    .update(updates)
                    .await()

                originalUsername = uiState.value.username
                originalShopName = uiState.value.shopName
                originalPhone = uiState.value.phone

                uiState.value = uiState.value.copy(
                    updateMessage = "Updated successfully"
                )
                isEditMode.value = false
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    updateMessage = "error: ${e.message}"
                )
            }
            isLoading.value = false
        }
    }

    fun clearUpdateMessage() {
        uiState.value = uiState.value.copy(updateMessage = null)
    }

    fun logout() {
        auth.signOut()
    }
}
