package ltdd.dacsba.groceries.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "BUYER",
    val shopName: String = "",
    val phone: String = "",
    // Trạng thái duyệt gian hàng: PENDING, APPROVED, REJECTED (chỉ dùng cho role SELLER)
    val sellerStatus: String = "",
    val isDeactivated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)