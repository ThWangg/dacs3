package ltdd.dacsba.groceries.data.model

data class Review(
    val reviewId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val rating: Double = 0.0,       // 1.0 → 5.0, bước 0.5
    val comment: String = "",
    val imageUrl: String = "",      // base64 hoặc URL (tùy chọn)
    val createdAt: Long = System.currentTimeMillis()
)
