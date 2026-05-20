package ltdd.dacsba.groceries.data.model

data class Order(
    val orderId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val shippingAddress: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String = "",
    val quantity: Int = 0,
    val priceAtOrder: Double = 0.0,
    val unit: String = ""
)

enum class OrderStatus(val displayName: String) {
    PENDING("Chờ xét duyệt"),
    CONFIRMED("Đã xác nhận"),
    SHIPPING("Đang giao hàng"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy")
}
