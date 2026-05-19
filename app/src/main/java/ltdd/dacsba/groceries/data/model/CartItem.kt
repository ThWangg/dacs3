package ltdd.dacsba.groceries.data.model

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val unit: String = "",
    val sellerId: String = ""
)
