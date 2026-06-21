package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.WalletTransaction
import ltdd.dacsba.groceries.data.repository.WalletRepository

class WalletViewModel : ViewModel() {
    private val walletRepository = WalletRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val walletBalance = mutableStateOf(0.0)
    val walletAccountNo = mutableStateOf("")     // Số tài khoản áo riêng của user
    val walletAccountName = mutableStateOf("")   // Tên hiển thị (= username)
    val transactions = mutableStateOf<List<WalletTransaction>>(emptyList())
    val isLoading = mutableStateOf(false)
    val isTopping = mutableStateOf(false)
    val message = mutableStateOf<String?>(null)

    // Real-time listener cho số dư
    private var balanceListener: ListenerRegistration? = null

    init {
        loadAll()
    }

    fun loadAll() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading.value = true
            // Lấy username từ Firestore
            val username = try {
                db.collection(AppConstant.COLLECTION_USERS)
                    .document(uid).get().await()
                    .getString("username") ?: ""
            } catch (_: Exception) { "" }

            // Tạo/lấy thông tin ví
            walletRepository.getOrCreateWallet(uid, username).onSuccess { (balance, accountNo, accountName) ->
                walletBalance.value = balance
                walletAccountNo.value = accountNo
                walletAccountName.value = accountName
            }
            walletRepository.getTransactions(uid).onSuccess { transactions.value = it }
            isLoading.value = false

            // Bắt đầu lắng nghe real-time
            listenBalance(uid)
        }
    }

    private fun listenBalance(uid: String) {
        balanceListener?.remove()
        balanceListener = db.collection(AppConstant.COLLECTION_WALLETS)
            .document(uid)
            .addSnapshotListener { snap, _ ->
                walletBalance.value = snap?.getDouble("balance") ?: 0.0
            }
    }

    override fun onCleared() {
        super.onCleared()
        balanceListener?.remove()
    }

    fun topUp(amount: Double, onSuccess: (Double) -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onError("Chưa đăng nhập"); return }
        if (amount <= 0) { onError("Số tiền không hợp lệ"); return }

        viewModelScope.launch {
            isTopping.value = true
            walletRepository.topUp(uid, amount, walletAccountName.value)
                .onSuccess { newBalance ->
                    // Real-time listener sẽ tự cập nhật balance, nhưng cũng refresh giao dịch
                    walletRepository.getTransactions(uid).onSuccess { transactions.value = it }
                    onSuccess(newBalance)
                }
                .onFailure { onError(it.message ?: "Lỗi nạp tiền") }
            isTopping.value = false
        }
    }

    fun clearMessage() { message.value = null }
}
