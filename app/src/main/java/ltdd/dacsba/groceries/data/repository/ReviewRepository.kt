package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Review

class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection(AppConstant.COLLECTION_REVIEWS)
    private val productsCollection = db.collection(AppConstant.COLLECTION_PRODUCTS)

    /**
     * Kiểm tra người dùng đã đánh giá sản phẩm này trong đơn hàng này chưa.
     */
    suspend fun hasReviewed(orderId: String, productId: String, buyerId: String): Boolean {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("productId", productId)
                .whereEqualTo("buyerId", buyerId)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Kiểm tra hàng loạt các cặp (orderId, productId) đã được review bởi buyerId.
     * Trả về Set của key "orderId_productId" đã được review.
     */
    suspend fun getReviewedKeys(buyerId: String, orderIds: List<String>): Set<String> {
        if (orderIds.isEmpty()) return emptySet()
        return try {
            val result = mutableSetOf<String>()
            // Firestore whereIn tối đa 10 phần tử
            orderIds.chunked(10).forEach { chunk ->
                val snapshot = reviewsCollection
                    .whereEqualTo("buyerId", buyerId)
                    .whereIn("orderId", chunk)
                    .get()
                    .await()
                snapshot.toObjects(Review::class.java).forEach { review ->
                    result.add("${review.orderId}_${review.productId}")
                }
            }
            result
        } catch (e: Exception) {
            emptySet()
        }
    }

    /**
     * Submit đánh giá và cập nhật ratingAverage, reviewCount trên sản phẩm bằng Transaction.
     */
    suspend fun submitReview(review: Review): Result<Boolean> {
        return try {
            // Tạo document id cho review
            val reviewRef = reviewsCollection.document()
            val reviewWithId = review.copy(reviewId = reviewRef.id)

            // Tìm document sản phẩm
            val productQuerySnap = productsCollection
                .whereEqualTo("id", review.productId)
                .get()
                .await()

            val productDocRef = if (!productQuerySnap.isEmpty) {
                productQuerySnap.documents.first().reference
            } else {
                // Fallback: dùng productId trực tiếp làm document id
                productsCollection.document(review.productId)
            }

            db.runTransaction { transaction ->
                val productSnap = transaction.get(productDocRef)
                val currentAvg = productSnap.getDouble("ratingAverage") ?: 0.0
                val currentCount = (productSnap.getLong("reviewCount") ?: 0L).toInt()

                val newCount = currentCount + 1
                val newAvg = ((currentAvg * currentCount) + review.rating) / newCount

                transaction.set(reviewRef, reviewWithId)
                transaction.update(productDocRef, mapOf(
                    "ratingAverage" to newAvg,
                    "reviewCount" to newCount
                ))
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách review của sản phẩm (tối đa 10 review mới nhất).
     */
    suspend fun getReviewsForProduct(productId: String, limit: Long = 10): Result<List<Review>> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("productId", productId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            Result.success(snapshot.toObjects(Review::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
