package ltdd.dacsba.groceries.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.User

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun registerUser(user: User, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID is null")

            val userWithUid = user.copy(uid = uid)
            db.collection(AppConstant.COLLECTION_USERS)
                .document(uid)
                .set(userWithUid)
                .await()

            Result.success(userWithUid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try{
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID is null")

            val document = db.collection(AppConstant.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()
            val userData = document.toObject(User::class.java)

            if (userData != null) {
                // Kiểm tra tài khoản có bị khóa không
                if (userData.isDeactivated) {
                    auth.signOut()  // Đăng xuất ngay khỏi Firebase Auth
                    throw Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.")
                }
                Result.success(userData)
            }
            else throw Exception("User data not found")
            }
        catch (e: Exception){
            Result.failure(e)
        }
    }

}