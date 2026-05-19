package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import java.util.UUID

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection(AppConstant.COLLECTION_ORDERS)

    suspend fun placeOrder(order: Order): Result<Boolean> {
        return try {
            val orderId = UUID.randomUUID().toString()
            val newOrder = order.copy(orderId = orderId)
            ordersCollection.document(orderId).set(newOrder).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
