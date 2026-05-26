package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.CartItem
import ltdd.dacsba.groceries.data.model.OrderStatus

/**
 * Xây dựng "hồ sơ sở thích tag" của user dựa trên:
 *  1. Lịch sử đơn hàng (orders collection)
 *  2. Giỏ hàng hiện tại (users/{uid}/cart sub-collection)
 *
 * Không cần collection mới — tái sử dụng dữ liệu đã có.
 */
class UserTagProfileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection(AppConstant.COLLECTION_PRODUCTS)
    private val ordersCollection = db.collection(AppConstant.COLLECTION_ORDERS)
    private val usersCollection = db.collection(AppConstant.COLLECTION_USERS)

    /**
     * Lấy danh sách productIds từ 5 đơn hàng gần nhất của user,
     * rồi query product để lấy tags.
     */
    suspend fun getTagsFromOrders(userId: String): List<String> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", userId)
                .get()
                .await()

            // S\u1eafp x\u1ebfp trong b\u1ed9 nh\u1edb \u2014 tr\u00e1nh c\u1ea7n composite index tr\u00ean Firestore
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

    /**
     * Lấy danh sách productIds từ giỏ hàng hiện tại,
     * rồi query product để lấy tags.
     */
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

    /**
     * Query Firestore để lấy tags của danh sách productIds.
     */
    private suspend fun fetchTagsForProducts(productIds: List<String>): List<String> {
        if (productIds.isEmpty()) return emptyList()
        val tags = mutableListOf<String>()
        // Batch theo chunk 10 (giới hạn Firestore whereIn)
        productIds.chunked(10).forEach { chunk ->
            try {
                val snap = productsCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()
                snap.documents.forEach { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val productTags = doc.get("tags") as? List<String> ?: emptyList()
                    tags.addAll(productTags)
                }
            } catch (_: Exception) {}
        }
        return tags
    }

    /**
     * Tổng hợp tags từ order + cart, đếm tần suất, trả về top tags.
     *
     * @return Danh sách tag sắp xếp theo tần suất giảm dần (tối đa [maxTags] tags)
     */
    suspend fun buildUserTagProfile(userId: String, maxTags: Int = 10): List<String> {
        val tagsFromOrders = getTagsFromOrders(userId)
        val tagsFromCart = if (tagsFromOrders.isEmpty()) getTagsFromCart(userId) else {
            // Vẫn bổ sung cart để đa dạng hơn, nhưng không fetch nếu order đã đủ nhiều
            if (tagsFromOrders.size < 5) getTagsFromCart(userId) else emptyList()
        }

        val allTags = tagsFromOrders + tagsFromCart
        if (allTags.isEmpty()) return emptyList()

        // Đếm tần suất
        val frequency = allTags.groupingBy { it }.eachCount()

        // Sắp xếp giảm dần theo tần suất → lấy top maxTags
        return frequency.entries
            .sortedByDescending { it.value }
            .take(maxTags)
            .map { it.key }
    }

    /**
     * Tập hợp danh sách các nhãn từ những sản phẩm mà người dùng đã mua thành công,
     * dựa trên lớp OrderItem của các đơn hàng Order có trạng thái status = DELIVERED
     */
    suspend fun getUserPreferredTags(userId: String): Set<String> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", userId)
                .get()
                .await()

            val orders = snapshot.documents
                .mapNotNull { it.toObject(Order::class.java) }
                .filter { it.status == OrderStatus.DELIVERED }

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
