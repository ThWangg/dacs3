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

suspend fun submitRequest(message: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")

val doc = db.collection(AppConstant.COLLECTION_USERS).document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("Không tìm thấy user")

val existing = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("uid", uid)
                .whereEqualTo("status", "PENDING")
                .get().await()
            if (!existing.isEmpty) {
                throw Exception("Bạn đã có yêu cầu đang chờ duyệt!")
            }

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

suspend fun getMyRequestStatus(): Result<String?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(null)
            val snapshot = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("uid", uid)
                .get().await()

            val status = snapshot.toObjects(SellerRequest::class.java)
                .sortedByDescending { it.createdAt }
                .firstOrNull()?.status
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

suspend fun getPendingRequests(): Result<List<SellerRequest>> {
        return try {
            val snapshot = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("status", "PENDING")
                .get().await()

            val sorted = snapshot.toObjects(SellerRequest::class.java)
                .sortedBy { it.createdAt }
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

suspend fun approveRequest(request: SellerRequest): Result<Boolean> {
        return try {
            val batch = db.batch()

            val reqRef = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS).document(request.requestId)
            batch.update(reqRef, "status", "APPROVED")

            val userRef = db.collection(AppConstant.COLLECTION_USERS).document(request.uid)
            batch.update(userRef, "role", "SELLER")
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
