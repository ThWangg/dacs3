package ltdd.dacsba.groceries.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "BUYER",
    @get:PropertyName("isDeactivated")
    @PropertyName("isDeactivated")
    val isDeactivated: Boolean = false,
    val avatarUrl: String = "",
    val walletBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)