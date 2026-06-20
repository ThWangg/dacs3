package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.CartItem
import ltdd.dacsba.groceries.data.model.OrderStatus

class UserTagProfileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection(AppConstant.COLLECTION_PRODUCTS)
    private val ordersCollection = db.collection(AppConstant.COLLECTION_ORDERS)
    private val usersCollection = db.collection(AppConstant.COLLECTION_USERS)

suspend fun getTagsFromOrders(userId: String): List<String> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", userId)
                .get()
                .await()

val orders = snapshot.documents
                .mapNotNull { it.toObject(Order::class.java) }
                .sortedByDescending { it.createdAt }
            val productIds = orders
                .flatMap { it.items }
                .map { it.productId }
                .filter { it.isNotBlank() }
                .distinct()

            fetchTagsForProducts(productIds)
        } catch (e: Exception) {
            emptyList()
        }
    }

suspend fun getTagsFromCart(userId: String): List<String> {
        return try {
            val snapshot = usersCollection
                .document(userId)
                .collection("cart")
                .get()
                .await()

            val cartItems = snapshot.toObjects(CartItem::class.java)
            val productIds = cartItems
                .map { it.productId }
                .filter { it.isNotBlank() }
                .distinct()

            fetchTagsForProducts(productIds)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchTagsForProducts(productIds: List<String>): List<String> {
        if (productIds.isEmpty()) return emptyList()
        val tagsMap = mutableMapOf<String, List<String>>()

        productIds.chunked(10).forEach { chunk ->
            try {
                val snap = productsCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()
                snap.documents.forEach { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val productTags = doc.get("tags") as? List<String> ?: emptyList()
                    val id = doc.getString("id") ?: doc.id
                    tagsMap[id] = productTags
                }
            } catch (_: Exception) {}
        }
        
        val tags = mutableListOf<String>()
        for (id in productIds) {
            tagsMap[id]?.let { tags.addAll(it) }
        }
        return tags
    }

    suspend fun buildUserTagProfile(userId: String, maxTags: Int = 10): List<String> {
        val tagsFromOrders = getTagsFromOrders(userId)
        val tagsFromCart = if (tagsFromOrders.isEmpty()) getTagsFromCart(userId) else {
            if (tagsFromOrders.size < 5) getTagsFromCart(userId) else emptyList()
        }

        val allTags = tagsFromOrders + tagsFromCart
        if (allTags.isEmpty()) return emptyList()

        // Count frequency but preserve first-seen order for ties
        val frequency = mutableMapOf<String, Int>()
        val firstSeenIndex = mutableMapOf<String, Int>()
        
        allTags.forEachIndexed { index, tag ->
            frequency[tag] = frequency.getOrDefault(tag, 0) + 1
            if (!firstSeenIndex.containsKey(tag)) {
                firstSeenIndex[tag] = index
            }
        }

        return frequency.keys
            .sortedWith(Comparator { t1, t2 ->
                val freqCompare = frequency[t2]!!.compareTo(frequency[t1]!!)
                if (freqCompare != 0) freqCompare
                else firstSeenIndex[t1]!!.compareTo(firstSeenIndex[t2]!!)
            })
            .take(maxTags)
    }

    suspend fun getUserPreferredTags(userId: String): Set<String> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", userId)
                .get()
                .await()

            val orders = snapshot.documents
                .mapNotNull { it.toObject(Order::class.java) }
                .filter { it.status != OrderStatus.CANCELLED }
                .sortedByDescending { it.createdAt }

            val productIds = orders
                .flatMap { it.items }
                .map { it.productId }
                .filter { it.isNotBlank() }
                .distinct()

            fetchTagsForProducts(productIds).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}
