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

/** Trạng thái thanh toán */
enum class PaymentStatus {
    LOADING,    // Đang tải thông tin seller
    WAITING,    // Đang chờ xác nhận
    SUCCESS,    // Thanh toán thành công
    FAILED      // Thất bại / hết giờ
}

/**
 * ViewModel quản lý màn hình thanh toán QR.
 *
 * Hai cơ chế xác nhận:
 * 1. [startListening] – addSnapshotListener Firestore: Tự động SUCCESS khi server/webhook ghi status="SUCCESS"
 * 2. [startCountdown] – Đếm ngược [COUNTDOWN_SECONDS]s rồi tự ghi SUCCESS vào Firestore (giả lập)
 * 3. [confirmManually] – Người dùng bấm nút "Đã chuyển khoản" → ghi SUCCESS ngay lập tức
 *
 * STK của seller được sinh **deterministic** từ sellerId hash:
 * cùng sellerId → luôn cho ra cùng một STK 12 chữ số (0–9), không random lại mỗi lần.
 */
class PaymentViewModel : ViewModel() {

    companion object {
        const val COUNTDOWN_SECONDS = 60  // Thời gian đếm ngược (giây)
        private const val STK_LENGTH = 12  // Độ dài số tài khoản

        /**
         * Sinh STK deterministic từ sellerId.
         * Seed bằng hashCode → luôn cho ra cùng dãy số với cùng sellerId.
         */
        fun generateSellerAccountNo(sellerId: String): String {
            val seed = abs(sellerId.hashCode()).toLong()
            val rng = java.util.Random(seed)
            return buildString {
                // Chữ số đầu không được là 0
                append(rng.nextInt(9) + 1)
                repeat(STK_LENGTH - 1) { append(rng.nextInt(10)) }
            }
        }
    }

    private val db = FirebaseFirestore.getInstance()

    // ── State flows ───────────────────────────────────────────────────────────
    private val _paymentStatus = MutableStateFlow(PaymentStatus.LOADING)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(COUNTDOWN_SECONDS)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

    /** Config QR của seller hiện tại (được load từ Firestore) */
    private val _qrConfig = MutableStateFlow(PaymentQrConfig.DEFAULT)
    val qrConfig: StateFlow<PaymentQrConfig> = _qrConfig.asStateFlow()

    // ── Internal ──────────────────────────────────────────────────────────────
    private var listenerRegistration: ListenerRegistration? = null
    private var countdownJob: Job? = null
    private var currentOrderId: String = ""

    /**
     * Load thông tin seller từ Firestore để tạo PaymentQrConfig per-seller.
     * - STK: sinh deterministic từ sellerId (cùng seller = cùng STK)
     * - Chủ TK: username của seller (tên cửa hàng)
     */
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
                // Fallback: vẫn dùng STK từ sellerId dù không lấy được tên
                _qrConfig.value = PaymentQrConfig(
                    accountNo = generateSellerAccountNo(sellerId),
                    accountName = "CUA HANG"
                )
            } finally {
                // Chuyển sang WAITING sau khi load xong
                if (_paymentStatus.value == PaymentStatus.LOADING) {
                    _paymentStatus.value = PaymentStatus.WAITING
                }
            }
        }
    }

    /**
     * Bắt đầu lắng nghe realtime từ Firestore.
     * Khi document payments/{orderId} có field status = "SUCCESS" → tự chuyển trạng thái.
     */
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

    /**
     * Bắt đầu đếm ngược. Khi hết giờ tự ghi SUCCESS vào Firestore (giả lập thanh toán).
     */
    fun startCountdown() {
        countdownJob?.cancel()
        _countdownSeconds.value = COUNTDOWN_SECONDS
        countdownJob = viewModelScope.launch {
            for (remaining in COUNTDOWN_SECONDS downTo 1) {
                if (_paymentStatus.value != PaymentStatus.WAITING) break
                _countdownSeconds.value = remaining
                delay(1000)
            }
            // Hết đếm ngược → Ghi SUCCESS vào Firestore (giả lập webhook)
            if (_paymentStatus.value == PaymentStatus.WAITING) {
                simulatePaymentSuccess()
            }
        }
    }

    /**
     * Người dùng bấm nút "Đã chuyển khoản" → ghi SUCCESS ngay vào Firestore
     */
    fun confirmManually() {
        if (_paymentStatus.value != PaymentStatus.WAITING) return
        simulatePaymentSuccess()
    }

    /**
     * Ghi document payments/{orderId} status="SUCCESS" vào Firestore.
     * Listener addSnapshotListener phía trên sẽ bắt được và tự update UI.
     */
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
                // Listener addSnapshotListener sẽ bắt được → _paymentStatus = SUCCESS
                _isConfirming.value = false
            }
            .addOnFailureListener {
                // Fallback: update UI trực tiếp nếu Firestore lỗi
                _paymentStatus.value = PaymentStatus.SUCCESS
                _isConfirming.value = false
            }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun onCleared() {
        listenerRegistration?.remove()   // Quan trọng: Dọn dẹp listener tránh memory leak
        countdownJob?.cancel()
        super.onCleared()
    }
}
