package ltdd.dacsba.groceries.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.SellerRequest
import ltdd.dacsba.groceries.data.model.User

class SellerRequestRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /** Buyer gửi yêu cầu trở thành Seller */
    suspend fun submitRequest(message: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")

            // Lấy thông tin user hiện tại
            val doc = db.collection(AppConstant.COLLECTION_USERS).document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("Không tìm thấy user")

            // Kiểm tra đã có yêu cầu PENDING chưa
            val existing = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("uid", uid)
                .whereEqualTo("status", "PENDING")
                .get().await()
            if (!existing.isEmpty) {
                throw Exception("Bạn đã có yêu cầu đang chờ duyệt!")
            }

            // Tạo yêu cầu mới
            val ref = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS).document()
            val request = SellerRequest(
                requestId = ref.id,
                uid = uid,
                username = user.username,
                email = user.email,
                avatarUrl = user.avatarUrl,
                message = message,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            ref.set(request).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Kiểm tra user hiện tại đã có yêu cầu PENDING chưa */
    suspend fun getMyRequestStatus(): Result<String?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(null)
            val snapshot = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("uid", uid)
                .get().await()
            // Sắp xếp ở client, lấy bản ghi mới nhất
            val status = snapshot.toObjects(SellerRequest::class.java)
                .sortedByDescending { it.createdAt }
                .firstOrNull()?.status
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Admin: lấy tất cả yêu cầu PENDING */
    suspend fun getPendingRequests(): Result<List<SellerRequest>> {
        return try {
            val snapshot = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("status", "PENDING")
                .get().await()
            // Sắp xếp ở client theo createdAt tăng dần
            val sorted = snapshot.toObjects(SellerRequest::class.java)
                .sortedBy { it.createdAt }
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Admin: duyệt yêu cầu → chuyển user sang SELLER */
    suspend fun approveRequest(request: SellerRequest): Result<Boolean> {
        return try {
            val batch = db.batch()
            // Cập nhật request status
            val reqRef = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS).document(request.requestId)
            batch.update(reqRef, "status", "APPROVED")
            // Cập nhật role user
            val userRef = db.collection(AppConstant.COLLECTION_USERS).document(request.uid)
            batch.update(userRef, "role", "SELLER")
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Admin: từ chối yêu cầu */
    suspend fun rejectRequest(requestId: String): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .document(requestId)
                .update("status", "REJECTED")
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
