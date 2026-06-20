package ltdd.dacsba.groceries.data.model

data class SellerRequest(
    val requestId: String = "",
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val message: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)
