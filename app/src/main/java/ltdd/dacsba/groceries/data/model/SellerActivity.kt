package ltdd.dacsba.groceries.data.model

import com.google.firebase.firestore.FirebaseFirestore

data class SellerActivity(
    val id: String = "",
    val sellerId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = ""
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
