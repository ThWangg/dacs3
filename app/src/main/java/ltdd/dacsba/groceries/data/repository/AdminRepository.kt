package ltdd.dacsba.groceries.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.model.SellerRequest
import ltdd.dacsba.groceries.data.model.User

class AdminRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentAdminEmail(): String = auth.currentUser?.email ?: "admin@tautshop.com"
    fun getCurrentAdminUid(): String = auth.currentUser?.uid ?: ""

    // ─── Users ───────────────────────────────────────────────────────────────

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = db.collection(AppConstant.COLLECTION_USERS).get().await()
            Result.success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun toggleUserDeactivate(uid: String, deactivate: Boolean): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_USERS)
                .document(uid).update("isDeactivated", deactivate).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateUserAvatar(uid: String, avatarUrl: String): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_USERS)
                .document(uid).update("avatarUrl", avatarUrl).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateAdminProfile(uid: String, username: String): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_USERS)
                .document(uid).update("username", username).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = db.collection(AppConstant.COLLECTION_PRODUCTS).get().await()
            Result.success(snapshot.toObjects(Product::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val ref = db.collection(AppConstant.COLLECTION_PRODUCTS).add(product).await()
            // ghi lại id vào document
            db.collection(AppConstant.COLLECTION_PRODUCTS)
                .document(ref.id).update("id", ref.id).await()
            Result.success(ref.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateProduct(product: Product): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_PRODUCTS)
                .document(product.id).set(product).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteProduct(productId: String): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_PRODUCTS)
                .document(productId).delete().await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Seed mẫu ─────────────────────────────────────────────────────────────

    suspend fun getProductCount(): Int {
        return try {
            db.collection(AppConstant.COLLECTION_PRODUCTS).get().await().size()
        } catch (e: Exception) { 0 }
    }

    suspend fun seedSampleProducts(): Result<Int> {
        return try {
            val sampleProducts = buildSampleProducts()
            val batch = db.batch()
            sampleProducts.forEachIndexed { index, product ->
                val ref = db.collection(AppConstant.COLLECTION_PRODUCTS).document()
                val withId = product.copy(id = ref.id)
                batch.set(ref, withId)
            }
            batch.commit().await()
            Result.success(sampleProducts.size)
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun buildSampleProducts(): List<Product> {
        val now = System.currentTimeMillis()
        return listOf(
            Product(name = "Táo Fuji Nhật", description = "Táo Fuji nhập khẩu từ Nhật Bản, giòn ngọt tự nhiên", price = 89000.0, unit = "kg", categoryId = "fresh_fruit", stock = 150, soldCount = 320, ratingAverage = 4.8, reviewCount = 65, tags = listOf("táo", "trái cây", "nhật bản"), imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400", createdAt = now - 86400000L * 7),
            Product(name = "Xoài Cát Hòa Lộc", description = "Xoài cát Hòa Lộc đặc sản miền Tây, thơm ngọt", price = 55000.0, unit = "kg", categoryId = "fresh_fruit", stock = 80, soldCount = 210, ratingAverage = 4.9, reviewCount = 48, tags = listOf("xoài", "trái cây", "miền tây"), imageUrl = "https://images.unsplash.com/photo-1553279768-865429fa0078?w=400", createdAt = now - 86400000L * 5),
            Product(name = "Cà Rốt Baby", description = "Cà rốt baby sạch, giàu vitamin A và chất xơ", price = 25000.0, unit = "bó", categoryId = "fresh_veggie", stock = 200, soldCount = 450, ratingAverage = 4.5, reviewCount = 92, tags = listOf("cà rốt", "rau củ", "sạch"), imageUrl = "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400", createdAt = now - 86400000L * 3),
            Product(name = "Rau Muống Sạch", description = "Rau muống trồng theo tiêu chuẩn VietGAP", price = 12000.0, unit = "bó", categoryId = "fresh_veggie", stock = 300, soldCount = 680, ratingAverage = 4.6, reviewCount = 120, tags = listOf("rau muống", "rau", "sạch"), imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400", createdAt = now - 86400000L * 2),
            Product(name = "Thịt Heo Ba Chỉ", description = "Thịt heo ba chỉ tươi, ngon từ trang trại sạch", price = 135000.0, unit = "kg", categoryId = "meat_seafood", stock = 60, soldCount = 185, ratingAverage = 4.7, reviewCount = 37, tags = listOf("thịt heo", "ba chỉ", "tươi"), imageUrl = "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400", createdAt = now - 86400000L * 6),
            Product(name = "Tôm Sú Tươi", description = "Tôm sú nuôi sạch, size 20-30 con/kg", price = 220000.0, unit = "kg", categoryId = "meat_seafood", stock = 40, soldCount = 95, ratingAverage = 4.8, reviewCount = 28, tags = listOf("tôm", "hải sản", "tươi"), imageUrl = "https://images.unsplash.com/photo-1565680018093-ebb6b9ab5460?w=400", createdAt = now - 86400000L * 4),
            Product(name = "Sữa TH True Milk", description = "Sữa tươi tiệt trùng TH True Milk 1 lít, không đường", price = 32000.0, unit = "hộp", categoryId = "dairy_egg", stock = 500, soldCount = 920, ratingAverage = 4.9, reviewCount = 210, tags = listOf("sữa", "th", "không đường"), imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", createdAt = now - 86400000L * 10),
            Product(name = "Trứng Gà Ta", description = "Trứng gà ta thả vườn, 10 quả/vỉ", price = 45000.0, unit = "vỉ", categoryId = "dairy_egg", stock = 250, soldCount = 430, ratingAverage = 4.7, reviewCount = 88, tags = listOf("trứng", "gà ta", "sạch"), imageUrl = "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400", createdAt = now - 86400000L * 1),
            Product(name = "Nước Suối Lavie 500ml", description = "Nước suối Lavie 500ml thùng 24 chai", price = 120000.0, unit = "thùng", categoryId = "beverage", stock = 180, soldCount = 560, ratingAverage = 4.5, reviewCount = 145, tags = listOf("nước suối", "lavie", "uống"), imageUrl = "https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400", createdAt = now - 86400000L * 8),
            Product(name = "Bánh Mì Sandwich", description = "Bánh mì sandwich nguyên cám, tốt cho sức khỏe", price = 28000.0, unit = "ổ", categoryId = "bakery", stock = 120, soldCount = 290, ratingAverage = 4.4, reviewCount = 63, tags = listOf("bánh mì", "sandwich", "nguyên cám"), imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400", createdAt = now - 86400000L * 9),
            Product(name = "Nước Mắm Phú Quốc", description = "Nước mắm Phú Quốc 40 độ đạm, chai 500ml", price = 68000.0, unit = "chai", categoryId = "condiment", stock = 95, soldCount = 340, ratingAverage = 4.8, reviewCount = 72, tags = listOf("nước mắm", "phú quốc", "gia vị"), imageUrl = "https://images.unsplash.com/photo-1526318896980-cf78c088247c?w=400", createdAt = now - 86400000L * 12),
            Product(name = "Bơ Sáp Đà Lạt", description = "Bơ sáp Đà Lạt chín đều, béo ngậy, giàu dinh dưỡng", price = 72000.0, unit = "kg", categoryId = "fresh_fruit", stock = 70, soldCount = 155, ratingAverage = 4.9, reviewCount = 41, tags = listOf("bơ", "đà lạt", "béo"), imageUrl = "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400", createdAt = now)
        )
    }

    // ─── Seller Requests ──────────────────────────────────────────────────────

    suspend fun getPendingRequests(): Result<List<SellerRequest>> {
        return try {
            val snapshot = db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .whereEqualTo("status", "PENDING")
                .get().await()
            // Sắp xếp ở client theo thời gian gửi tăng dần
            val sorted = snapshot.toObjects(SellerRequest::class.java)
                .sortedBy { it.createdAt }
            Result.success(sorted)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun approveSellerRequest(request: SellerRequest): Result<Boolean> {
        return try {
            val batch = db.batch()
            batch.update(
                db.collection(AppConstant.COLLECTION_SELLER_REQUESTS).document(request.requestId),
                "status", "APPROVED"
            )
            batch.update(
                db.collection(AppConstant.COLLECTION_USERS).document(request.uid),
                "role", "SELLER"
            )
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectSellerRequest(requestId: String): Result<Boolean> {
        return try {
            db.collection(AppConstant.COLLECTION_SELLER_REQUESTS)
                .document(requestId).update("status", "REJECTED").await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun logout() = auth.signOut()
}
