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
import ltdd.dacsba.groceries.data.repository.OrderRepository
import ltdd.dacsba.groceries.data.model.OrderStatus
import ltdd.dacsba.groceries.data.model.SellerActivity

class SellerViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var products = mutableStateOf<List<Product>>(emptyList())
    var orders = mutableStateOf<List<Order>>(emptyList())
    var isLoading = mutableStateOf(false)
    var message = mutableStateOf<String?>(null)

var addSuccess = mutableStateOf(false)
    var updateSuccess = mutableStateOf(false)

var totalProducts = mutableStateOf(0)
    var totalSold = mutableStateOf(0)
    var avgRating = mutableStateOf(0.0)
    var totalStock = mutableStateOf(0)
    var activities = mutableStateOf<List<SellerActivity>>(emptyList())

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

val result = productRepository.getSellerProductsCount(currentUserId)
            result.onSuccess { list ->
                products.value = list
                totalProducts.value = list.size
                totalSold.value = list.sumOf { it.soldCount }
                totalStock.value = list.sumOf { it.stock }
                avgRating.value = if (list.isNotEmpty()) {
                    list.map { it.ratingAverage }.average()
                } else 0.0
            }
            result.onFailure { error ->
                message.value = error.message
            }

loadOrders(currentUserId)

loadActivities(currentUserId)

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

    private suspend fun loadActivities(sellerId: String) {
        try {
            val snapshot = db.collection("seller_activities")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()
            activities.value = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SellerActivity::class.java)
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            message.value = "Lỗi tải hoạt động: ${e.message}"
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            isLoading.value = true
            val result = productRepository.addProduct(product)
            result.onSuccess {
                addSuccess.value = true
                SellerActivity.log(product.sellerId, "Thêm sản phẩm", "Đã thêm sản phẩm mới \"${product.name}\"", "ADD_PRODUCT")
                refreshData()
            }.onFailure { message.value = it.message }
            isLoading.value = false
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            isLoading.value = true
            updateSuccess.value = false
            try {
                val oldProductDoc = db.collection(AppConstant.COLLECTION_PRODUCTS).document(product.id).get().await()
                val oldProduct = oldProductDoc.toObject(Product::class.java)
                
                val result = productRepository.updateProduct(product)
                result.onSuccess {
                    updateSuccess.value = true
                    
                    if (oldProduct != null) {
                        val changes = mutableListOf<String>()
                        if (oldProduct.name != product.name) changes.add("tên")
                        if (oldProduct.price != product.price) changes.add("giá")
                        if (oldProduct.stock != product.stock) changes.add("tồn kho")
                        if (oldProduct.description != product.description) changes.add("mô tả")
                        if (oldProduct.imageUrl != product.imageUrl) changes.add("ảnh")
                        
                        val msg = if (changes.isNotEmpty()) {
                            "Đã sửa ${changes.joinToString(", ")} của sản phẩm \"${product.name}\""
                        } else {
                            "Đã cập nhật thông tin sản phẩm \"${product.name}\""
                        }
                        SellerActivity.log(product.sellerId, "Sửa sản phẩm", msg, "EDIT_PRODUCT")
                    }
                    refreshData()
                }.onFailure { message.value = it.message }
            } catch (e: Exception) {
                message.value = e.message
            }
            isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val prodDoc = db.collection(AppConstant.COLLECTION_PRODUCTS).document(productId).get().await()
                val prodName = prodDoc.getString("name") ?: "Sản phẩm"
                val sellerId = prodDoc.getString("sellerId") ?: ""
                
                val result = productRepository.deleteProduct(productId)
                result.onSuccess {
                    if (sellerId.isNotBlank()) {
                        SellerActivity.log(sellerId, "Xóa sản phẩm", "Đã xóa sản phẩm \"$prodName\"", "DELETE_PRODUCT")
                    }
                    refreshData()
                }.onFailure { message.value = it.message }
            } catch (e: Exception) {
                message.value = e.message
            }
            isLoading.value = false
        }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                when (newStatus) {
                    OrderStatus.CANCELLED -> {
                        val result = orderRepository.cancelOrderAndRestoreStock(orderId)
                        if (result.isSuccess) {
                            SellerActivity.log(currentUserId, "Hủy đơn hàng", "Đã hủy đơn hàng #$orderId", "ORDER_CANCEL")
                            refreshData()
                            onSuccess()
                        } else {
                            onError(result.exceptionOrNull()?.message ?: "Không thể hủy đơn hàng")
                        }
                    }
                    OrderStatus.DELIVERED -> {

                        db.runTransaction { transaction ->
                            val orderRef = db.collection(AppConstant.COLLECTION_ORDERS).document(orderId)
                            val orderSnapshot = transaction.get(orderRef)
                            val order = orderSnapshot.toObject(Order::class.java)
                                ?: throw Exception("Không thể đọc thông tin đơn hàng")

val soldUpdates = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Int>>()
                            for (item in order.items) {
                                val productRef = db.collection(AppConstant.COLLECTION_PRODUCTS).document(item.productId)
                                val prodSnapshot = transaction.get(productRef)
                                if (prodSnapshot.exists()) {
                                    val currentSoldCount = prodSnapshot.getLong("soldCount") ?: 0L
                                    val newSoldCount = (currentSoldCount + item.quantity).toInt()
                                    soldUpdates.add(productRef to newSoldCount)
                                }
                            }

for ((productRef, newSoldCount) in soldUpdates) {
                                transaction.update(productRef, "soldCount", newSoldCount)
                            }

transaction.update(orderRef, mapOf(
                                "status" to OrderStatus.DELIVERED.name,
                                "updatedAt" to System.currentTimeMillis()
                            ))
                            null
                        }.await()
                        SellerActivity.log(currentUserId, "Đơn hàng đã giao", "Đơn hàng #$orderId đã giao thành công", "ORDER_DELIVERED")
                        refreshData()
                        onSuccess()
                    }
                    else -> {
                        db.collection(AppConstant.COLLECTION_ORDERS)
                            .document(orderId)
                            .update(
                                mapOf(
                                    "status" to newStatus.name,
                                    "updatedAt" to System.currentTimeMillis()
                                )
                            )
                            .await()
                        val actTitle = if (newStatus == OrderStatus.CONFIRMED) "Xác nhận đơn" else "Giao hàng"
                        val actMsg = if (newStatus == OrderStatus.CONFIRMED) "Đã xác nhận đơn hàng #$orderId" else "Đơn hàng #$orderId đang được vận chuyển"
                        val actType = if (newStatus == OrderStatus.CONFIRMED) "ORDER_CONFIRM" else "ORDER_SHIPPING"
                        SellerActivity.log(currentUserId, actTitle, actMsg, actType)
                        refreshData()
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Không thể cập nhật trạng thái")
            }
        }
    }
}