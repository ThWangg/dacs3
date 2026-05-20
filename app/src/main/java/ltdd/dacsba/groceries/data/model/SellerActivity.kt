package ltdd.dacsba.groceries.data.model

import com.google.firebase.firestore.FirebaseFirestore

data class SellerActivity(
    val id: String = "",
    val sellerId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "" // "UPDATE_PROFILE", "UPDATE_AVATAR", "REMOVE_AVATAR", "ORDER_CONFIRM", "ORDER_SHIPPING", "ORDER_DELIVERED", "ORDER_CANCEL", "ADD_PRODUCT", "EDIT_PRODUCT"
) {
    companion object {
        fun log(sellerId: String, title: String, message: String, type: String) {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("seller_activities").document()
            val activity = SellerActivity(
                id = ref.id,
                sellerId = sellerId,
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type
            )
            ref.set(activity)
        }
    }
}
