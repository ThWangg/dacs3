package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.repository.ProductRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SellerStatsViewModel : ViewModel() {

    enum class StatsPeriod(val displayName: String) {
        WEEK("7 ngày"),
        MONTH("30 ngày"),
        ALL("Tất cả")
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val productRepository = ProductRepository()

    var selectedPeriod = mutableStateOf(StatsPeriod.WEEK)
        private set

    // Dữ liệu cho bar chart: Pair(nhãn ngày, doanh thu)
    var revenueByDay = mutableStateOf<List<Pair<String, Double>>>(emptyList())
        private set

    // Top 5 sản phẩm bán chạy
    var topSellingProducts = mutableStateOf<List<Product>>(emptyList())
        private set

    var totalRevenue = mutableStateOf(0.0)
        private set

    var totalOrderCount = mutableStateOf(0)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        loadStats()
    }

    fun onPeriodSelected(period: StatsPeriod) {
        selectedPeriod.value = period
        loadStats()
    }

    fun loadStats() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading.value = true
            loadRevenueStats(currentUserId)
            loadTopProducts(currentUserId)
            isLoading.value = false
        }
    }

    private suspend fun loadRevenueStats(sellerId: String) {
        try {
            val cutoffTimeMillis = when (selectedPeriod.value) {
                StatsPeriod.WEEK -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                StatsPeriod.MONTH -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                StatsPeriod.ALL -> 0L
            }

            val snapshot = db.collection(AppConstant.COLLECTION_ORDERS)
                .whereEqualTo("sellerId", sellerId)
                .whereGreaterThan("createdAt", cutoffTimeMillis)
                .get()
                .await()

            val orders = snapshot.toObjects(Order::class.java)
            totalRevenue.value = orders.sumOf { it.totalAmount }
            totalOrderCount.value = orders.size

            // Nhóm doanh thu theo ngày
            val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
            val grouped = orders.groupBy { order ->
                dateFormatter.format(Date(order.createdAt))
            }

            revenueByDay.value = grouped.map { (day, dayOrders) ->
                day to dayOrders.sumOf { it.totalAmount }
            }.sortedBy { it.first }

        } catch (e: Exception) {
            errorMessage.value = "Lỗi tải doanh thu: ${e.message}"
        }
    }

    private suspend fun loadTopProducts(sellerId: String) {
        val result = productRepository.getSellerProductsCount(sellerId)
        result.onSuccess { products ->
            topSellingProducts.value = products
                .sortedByDescending { it.soldCount }
                .take(5)
        }.onFailure { error ->
            errorMessage.value = "Lỗi tải sản phẩm: ${error.message}"
        }
    }
}
