package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.OrderStatus
import java.util.UUID

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection(AppConstant.COLLECTION_ORDERS)

    suspend fun placeOrder(order: Order): Result<Boolean> {
        return try {
            val orderId = UUID.randomUUID().toString()
            val newOrder = order.copy(orderId = orderId)
            
            db.runTransaction { transaction ->
                // Phase 1: All Reads and validations
                val stockUpdates = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Int>>()
                for (item in order.items) {
                    val productRef = db.collection(AppConstant.COLLECTION_PRODUCTS).document(item.productId)
                    val snapshot = transaction.get(productRef)
                    if (!snapshot.exists()) {
                        throw Exception("Sản phẩm ${item.productName} không tồn tại")
                    }
                    val currentStock = snapshot.getLong("stock") ?: 0L
                    if (currentStock < item.quantity) {
                        throw Exception("Sản phẩm ${item.productName} không đủ tồn kho (Còn lại: $currentStock)")
                    }
                    
                    val newStock = (currentStock - item.quantity).toInt()
                    stockUpdates.add(productRef to newStock)
                }
                
                // Phase 2: All Writes
                for ((productRef, newStock) in stockUpdates) {
                    transaction.update(productRef, "stock", newStock)
                }
                
                // 2. Lưu đơn hàng mới
                val orderRef = ordersCollection.document(orderId)
                transaction.set(orderRef, newOrder)
                null
            }.await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBuyerOrders(buyerId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", buyerId)
                .get()
                .await()
            val orders = snapshot.documents
                .mapNotNull { it.toObject(Order::class.java) }
                .sortedByDescending { it.createdAt }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrderAndRestoreStock(orderId: String): Result<Boolean> {
        return try {
            db.runTransaction { transaction ->
                val orderRef = ordersCollection.document(orderId)
                val orderSnapshot = transaction.get(orderRef)
                if (!orderSnapshot.exists()) {
                    throw Exception("Đơn hàng không tồn tại")
                }
                val order = orderSnapshot.toObject(Order::class.java) ?: throw Exception("Không thể đọc thông tin đơn hàng")
                
                // Nếu đơn hàng đã hủy rồi thì không cần hoàn trả nữa
                if (order.status == OrderStatus.CANCELLED) {
                    return@runTransaction null
                }
                
                // Phase 1: All Reads
                val stockUpdates = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Int>>()
                for (item in order.items) {
                    val productRef = db.collection(AppConstant.COLLECTION_PRODUCTS).document(item.productId)
                    val prodSnapshot = transaction.get(productRef)
                    if (prodSnapshot.exists()) {
                        val currentStock = prodSnapshot.getLong("stock") ?: 0L
                        val newStock = (currentStock + item.quantity).toInt()
                        stockUpdates.add(productRef to newStock)
                    }
                }
                
                // Phase 2: All Writes
                for ((productRef, newStock) in stockUpdates) {
                    transaction.update(productRef, "stock", newStock)
                }
                
                // 2. Cập nhật trạng thái đơn hàng sang CANCELLED
                transaction.update(orderRef, "status", OrderStatus.CANCELLED.name)
                transaction.update(orderRef, "updatedAt", System.currentTimeMillis())
                null
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

