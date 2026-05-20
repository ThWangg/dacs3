package ltdd.dacsba.groceries.ui.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.data.repository.AuthRepository



class AuthViewModel: ViewModel() {
    private val authRepository = AuthRepository()

    var isLoading = mutableStateOf(false)
    var message = mutableStateOf<String?>(null)
    var loginSuccess = mutableStateOf<User?>(null)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            message.value = null

            val result = authRepository.loginUser(email, password)

            result.onSuccess { user ->
                loginSuccess.value = user
            }
            result.onFailure { error ->
                message.value = error.message
            }

            isLoading.value = false
        }
    }

    fun register(user: User, pass: String) {
        viewModelScope.launch {
            isLoading.value = true
            val result = authRepository.registerUser(user, pass)

            result.onSuccess { registeredUser ->
                loginSuccess.value = registeredUser
                message.value = "Đăng ký thành công!"
                isLoading.value = false
            }.onFailure { e ->
                message.value = e.message
                isLoading.value = false
            }
        }
    }

    var autoLoginChecking = mutableStateOf(false)

    fun checkAutoLogin(onNavigate: (User) -> Unit) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }
        viewModelScope.launch {
            autoLoginChecking.value = true
            message.value = null
            try {
                val document = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection(ltdd.dacsba.groceries.data.constant.AppConstant.COLLECTION_USERS)
                    .document(currentUser.uid)
                    .get()
                    .await()
                val userData = document.toObject(User::class.java)
                if (userData != null) {
                    if (userData.isDeactivated) {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        message.value = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin."
                    } else {
                        onNavigate(userData)
                    }
                } else {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                }
            } catch (e: Exception) {
                // Lỗi kết nối hoặc lỗi Firestore, đăng xuất để đảm bảo an toàn
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } finally {
                autoLoginChecking.value = false
            }
        }
    }
}



