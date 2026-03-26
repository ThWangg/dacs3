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


class SellerViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var products = mutableStateOf<List<Product>>(emptyList())
    var orders = mutableStateOf<List<Order>>(emptyList())
    var isLoading = mutableStateOf(false)
    var message = mutableStateOf<String?>(null)

    // status
    var addSuccess = mutableStateOf(false)
    var updateSuccess = mutableStateOf(false)

    // dashboard
    var totalProducts = mutableStateOf(0)
    var totalSold = mutableStateOf(0)
    var avgRating = mutableStateOf(0.0)

    init {
        refreshData()
    }

    fun refreshData() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            message.value = "Not logged in"
            return
        }

        viewModelScope.launch {
            isLoading.value = true

            // Load products
            val result = productRepository.getSellerProductsCount(currentUserId)
            result.onSuccess { list ->
                products.value = list
                totalProducts.value = list.size
                totalSold.value = list.sumOf { it.soldCount }
                avgRating.value = if (list.isNotEmpty()) {
                    list.map { it.ratingAverage }.average()
                } else 0.0
            }
            result.onFailure { error ->
                message.value = error.message
            }

            // Load orders của seller này
            loadOrders(currentUserId)

            isLoading.value = false
        }
    }

    private suspend fun loadOrders(sellerId: String) {
        try {
            val snapshot = db.collection(AppConstant.COLLECTION_ORDERS)
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()
            orders.value = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }
        } catch (e: Exception) {
            message.value = "Lỗi tải đơn hàng: ${e.message}"
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            isLoading.value = true
            val result = productRepository.addProduct(product)
            result.onSuccess {
                addSuccess.value = true
                refreshData()
            }.onFailure { message.value = it.message }
            isLoading.value = false
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            isLoading.value = true
            updateSuccess.value = false
            val result = productRepository.updateProduct(product)
            result.onSuccess {
                updateSuccess.value = true
                refreshData()
            }.onFailure { message.value = it.message }
            isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            isLoading.value = true
            val result = productRepository.deleteProduct(productId)
            result.onSuccess {
                refreshData()
            }.onFailure { message.value = it.message }
            isLoading.value = false
        }
    }
}