package ltdd.dacsba.groceries.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val imageUrl: String = "",
    val categoryId: String = "",
    val stock: Int = 0,
    val sellerId: String = "",
    val soldCount: Int = 0,
    val status: String = "APPROVED", // "PENDING", "APPROVED", "REJECTED"


    //recommendation system
    val tags: List<String> = emptyList(),
    val ratingAverage: Double = 0.0,
    val reviewCount: Int = 0,
    val categorySoldCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)