package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.CartItem

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection(AppConstant.COLLECTION_USERS)

    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
        return try {
            val snapshot = usersCollection.document(userId).collection("cart").get().await()
            val items = snapshot.toObjects(CartItem::class.java)
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToCart(userId: String, item: CartItem): Result<Boolean> {
        return try {
            val cartRef = usersCollection.document(userId).collection("cart").document(item.productId)
            val doc = cartRef.get().await()
            if (doc.exists()) {
                val currentQuantity = doc.getLong("quantity") ?: 0
                cartRef.update("quantity", currentQuantity + item.quantity).await()
            } else {
                cartRef.set(item).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(userId: String, productId: String): Result<Boolean> {
        return try {
            usersCollection.document(userId).collection("cart").document(productId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearCart(userId: String): Result<Boolean> {
        return try {
            val snapshot = usersCollection.document(userId).collection("cart").get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
