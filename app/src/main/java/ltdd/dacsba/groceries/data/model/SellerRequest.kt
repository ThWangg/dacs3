package ltdd.dacsba.groceries.data.model

data class SellerRequest(
    val requestId: String = "",
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val message: String = "",         // Lý do muốn trở thành seller
    val status: String = "PENDING",   // PENDING | APPROVED | REJECTED
    val createdAt: Long = System.currentTimeMillis()
)
