package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.model.User

class AdminRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection(AppConstant.COLLECTION_USERS)
    private val categoryCollection = db.collection(AppConstant.COLLECTION_CATEGORIES)

    // ======== UC01: Quản lý người dùng ========

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = userCollection
                .orderBy("createdAt")
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleUserDeactivation(userId: String, isDeactivated: Boolean): Result<Boolean> {
        return try {
            userCollection.document(userId)
                .update("isDeactivated", isDeactivated)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======== UC02: Kiểm duyệt gian hàng ========

    suspend fun getPendingSellerRequests(): Result<List<User>> {
        return try {
            val snapshot = userCollection
                .whereEqualTo("sellerStatus", AppConstant.Roles.SELLER_STATUS_PENDING)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveSellerRequest(userId: String): Result<Boolean> {
        return try {
            val updates = mapOf(
                "role" to AppConstant.Roles.SELLER,
                "sellerStatus" to AppConstant.Roles.SELLER_STATUS_APPROVED
            )
            userCollection.document(userId).update(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectSellerRequest(userId: String): Result<Boolean> {
        return try {
            val updates = mapOf(
                "sellerStatus" to AppConstant.Roles.SELLER_STATUS_REJECTED
            )
            userCollection.document(userId).update(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======== UC03: Quản lý danh mục ========

    suspend fun getAllCategories(): Result<List<Category>> {
        return try {
            val snapshot = categoryCollection.get().await()
            if (snapshot.isEmpty) {
                // Nếu chưa có danh mục trên Firestore, trả về danh sách mặc định
                Result.success(Category.defaultCategories)
            } else {
                val categories = snapshot.toObjects(Category::class.java)
                Result.success(categories)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOrUpdateCategory(category: Category): Result<Boolean> {
        return try {
            categoryCollection.document(category.categoryId).set(category).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Boolean> {
        return try {
            categoryCollection.document(categoryId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
