package ltdd.dacsba.groceries.ui.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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
}


