package ltdd.dacsba.groceries.ui.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.PaymentQrConfig
import kotlin.math.abs

enum class PaymentStatus {
    LOADING,
    WAITING,
    SUCCESS,
    FAILED
}

class PaymentViewModel : ViewModel() {

    companion object {
        const val COUNTDOWN_SECONDS = 60
        private const val STK_LENGTH = 12

fun generateSellerAccountNo(sellerId: String): String {
            val seed = abs(sellerId.hashCode()).toLong()
            val rng = java.util.Random(seed)
            return buildString {

                append(rng.nextInt(9) + 1)
                repeat(STK_LENGTH - 1) { append(rng.nextInt(10)) }
            }
        }
    }

    private val db = FirebaseFirestore.getInstance()

private val _paymentStatus = MutableStateFlow(PaymentStatus.LOADING)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(COUNTDOWN_SECONDS)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

private val _qrConfig = MutableStateFlow(PaymentQrConfig.DEFAULT)
    val qrConfig: StateFlow<PaymentQrConfig> = _qrConfig.asStateFlow()

private var listenerRegistration: ListenerRegistration? = null
    private var countdownJob: Job? = null
    private var currentOrderId: String = ""

fun loadSellerConfig(sellerId: String) {
        viewModelScope.launch {
            try {
                val sellerDoc = db.collection(AppConstant.COLLECTION_USERS)
                    .document(sellerId)
                    .get()
                    .await()

                val shopName = sellerDoc.getString("username") ?: "CUA HANG"
                val accountNo = generateSellerAccountNo(sellerId)

                _qrConfig.value = PaymentQrConfig(
                    bankId = "MB",
                    accountNo = accountNo,
                    accountName = shopName.uppercase().trim(),
                    template = "compact2"
                )
            } catch (e: Exception) {

                _qrConfig.value = PaymentQrConfig(
                    accountNo = generateSellerAccountNo(sellerId),
                    accountName = "CUA HANG"
                )
            } finally {

                if (_paymentStatus.value == PaymentStatus.LOADING) {
                    _paymentStatus.value = PaymentStatus.WAITING
                }
            }
        }
    }

fun startListening(orderId: String) {
        currentOrderId = orderId
        listenerRegistration?.remove()
        listenerRegistration = db
            .collection(AppConstant.COLLECTION_PAYMENTS)
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    if (status == "SUCCESS" && _paymentStatus.value == PaymentStatus.WAITING) {
                        _paymentStatus.value = PaymentStatus.SUCCESS
                        cancelCountdown()
                    }
                }
            }
    }

fun startCountdown() {
        countdownJob?.cancel()
        _countdownSeconds.value = COUNTDOWN_SECONDS
        countdownJob = viewModelScope.launch {
            for (remaining in (COUNTDOWN_SECONDS - 1) downTo 0) {
                delay(1000)
                if (_paymentStatus.value != PaymentStatus.WAITING) break
                _countdownSeconds.value = remaining
            }

            if (_paymentStatus.value == PaymentStatus.WAITING) {
                simulatePaymentSuccess()
            }
        }
    }

fun confirmManually() {
        if (_paymentStatus.value != PaymentStatus.WAITING) return
        simulatePaymentSuccess()
    }

private fun simulatePaymentSuccess() {
        if (currentOrderId.isBlank()) {
            _paymentStatus.value = PaymentStatus.SUCCESS
            return
        }
        _isConfirming.value = true
        db.collection(AppConstant.COLLECTION_PAYMENTS)
            .document(currentOrderId)
            .set(
                mapOf(
                    "orderId" to currentOrderId,
                    "status" to "SUCCESS",
                    "paidAt" to System.currentTimeMillis(),
                    "method" to "VIETQR_SIMULATED"
                )
            )
            .addOnSuccessListener {

                _isConfirming.value = false
            }
            .addOnFailureListener {

                _paymentStatus.value = PaymentStatus.SUCCESS
                _isConfirming.value = false
            }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        countdownJob?.cancel()
        super.onCleared()
    }
}
