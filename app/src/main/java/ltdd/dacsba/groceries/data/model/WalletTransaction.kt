package ltdd.dacsba.groceries.data.model

data class WalletTransaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "",          // "TOPUP" | "PAYMENT"
    val amount: Double = 0.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
