package ltdd.dacsba.groceries.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "BUYER",
    val isDeactivated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)