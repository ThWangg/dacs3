package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.WalletTransaction

class WalletRepository {
    private val db = FirebaseFirestore.getInstance()
    private val wallets = db.collection(AppConstant.COLLECTION_WALLETS)
    private val transactions = db.collection(AppConstant.COLLECTION_WALLET_TRANSACTIONS)

    /**
     * Tự sinh số tài khoản ví 10 chữ số từ userId (deterministic).
     * Luôn bắt đầu bằng "9" để trông giống virtual account.
     */
    fun generateAccountNo(userId: String): String {
        val hash = userId.fold(0L) { acc, c -> acc * 31 + c.code }
        val abs = if (hash < 0) -hash else hash
        return "9" + (abs % 1_000_000_000L).toString().padStart(9, '0')
    }

    /**
     * Khởi tạo ví nếu chưa có, trả về (balance, accountNo, accountName).
     * Sẽ được gọi lần đầu tiên khi user vào màn hình ví.
     */
    suspend fun getOrCreateWallet(
        userId: String,
        username: String
    ): Result<Triple<Double, String, String>> {
        return try {
            val ref = wallets.document(userId)
            val snap = ref.get().await()
            return if (snap.exists()) {
                val balance = snap.getDouble("balance") ?: 0.0
                val accountNo = snap.getString("accountNo") ?: generateAccountNo(userId)
                val accountName = snap.getString("accountName") ?: username
                Result.success(Triple(balance, accountNo, accountName))
            } else {
                // Tạo ví mới
                val accountNo = generateAccountNo(userId)
                val data = mapOf(
                    "balance" to 0.0,
                    "userId" to userId,
                    "accountNo" to accountNo,
                    "accountName" to username
                )
                ref.set(data).await()
                // Đồng bộ walletBalance về User document
                db.collection(AppConstant.COLLECTION_USERS).document(userId)
                    .update("walletBalance", 0.0).await()
                Result.success(Triple(0.0, accountNo, username))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Lấy số dư ví hiện tại */
    suspend fun getBalance(userId: String): Result<Double> {
        return try {
            val doc = wallets.document(userId).get().await()
            Result.success(doc.getDouble("balance") ?: 0.0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Nạp tiền vào ví (simulation – người dùng tự xác nhận).
     * Dùng Firestore transaction để đảm bảo atomic.
     */
    suspend fun topUp(
        userId: String,
        amount: Double,
        username: String = ""
    ): Result<Double> {
        return try {
            var newBalance = 0.0
            val accountNo = generateAccountNo(userId)
            db.runTransaction { tx ->
                val ref = wallets.document(userId)
                val snap = tx.get(ref)
                val current = snap.getDouble("balance") ?: 0.0
                newBalance = current + amount
                val data = mutableMapOf<String, Any>(
                    "balance" to newBalance,
                    "userId" to userId,
                    "accountNo" to (snap.getString("accountNo") ?: accountNo)
                )
                if (username.isNotBlank()) data["accountName"] = username
                tx.set(ref, data)
            }.await()

            // Ghi lịch sử giao dịch
            val txRef = transactions.document()
            txRef.set(
                WalletTransaction(
                    id = txRef.id,
                    userId = userId,
                    type = "TOPUP",
                    amount = amount,
                    note = "Nạp tiền vào ví",
                    createdAt = System.currentTimeMillis()
                )
            ).await()

            // Đồng bộ walletBalance vào User document
            db.collection(AppConstant.COLLECTION_USERS)
                .document(userId)
                .update("walletBalance", newBalance)
                .await()

            Result.success(newBalance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Trừ tiền buyer và cộng tiền seller khi thanh toán bằng ví.
     * Atomic: cả 2 thao tác trong cùng 1 Firestore transaction.
     */
    suspend fun deduct(
        userId: String,
        amount: Double,
        note: String,
        sellerId: String = ""
    ): Result<Double> {
        return try {
            var newBuyerBalance = 0.0
            db.runTransaction { tx ->
                val buyerRef = wallets.document(userId)

                // ── TẤT CẢ READS TRƯỚC ──
                val buyerSnap = tx.get(buyerRef)
                val sellerRef = if (sellerId.isNotBlank()) wallets.document(sellerId) else null
                val sellerSnap = sellerRef?.let { tx.get(it) }

                // ── SAU ĐÓ MỚI WRITES ──
                val buyerBalance = buyerSnap.getDouble("balance") ?: 0.0
                if (buyerBalance < amount) throw Exception("Số dư ví không đủ")
                
                if (sellerRef != null && sellerId != userId) {
                    newBuyerBalance = buyerBalance - amount
                    // Cập nhật số dư buyer bằng update để bảo toàn accountNo và accountName
                    tx.update(buyerRef, "balance", newBuyerBalance)

                    if (sellerSnap != null && sellerSnap.exists()) {
                        val sellerBalance = sellerSnap.getDouble("balance") ?: 0.0
                        val newSellerBalance = sellerBalance + amount
                        tx.update(sellerRef, "balance", newSellerBalance)
                    } else {
                        // Nếu ví seller chưa được tạo, khởi tạo ví mới tránh lỗi
                        val sellerAccountNo = generateAccountNo(sellerId)
                        val data = mapOf(
                            "balance" to amount,
                            "userId" to sellerId,
                            "accountNo" to sellerAccountNo,
                            "accountName" to "Chủ cửa hàng"
                        )
                        tx.set(sellerRef, data)
                    }
                } else if (sellerRef != null && sellerId == userId) {
                    // Nếu tự mua sản phẩm của chính mình (cùng tài khoản), số dư giữ nguyên (net-zero)
                    newBuyerBalance = buyerBalance
                    tx.update(buyerRef, "balance", newBuyerBalance)
                } else {
                    // Nếu sản phẩm không có sellerId (như sample products), chỉ trừ tiền buyer
                    newBuyerBalance = buyerBalance - amount
                    tx.update(buyerRef, "balance", newBuyerBalance)
                }
            }.await()

            // Ghi lịch sử buyer
            val txRef = transactions.document()
            txRef.set(
                WalletTransaction(
                    id = txRef.id,
                    userId = userId,
                    type = "PAYMENT",
                    amount = amount,
                    note = note,
                    createdAt = System.currentTimeMillis()
                )
            ).await()

            // Ghi lịch sử seller nếu có (chỉ ghi khi seller khác buyer)
            if (sellerId.isNotBlank() && sellerId != userId) {
                val stxRef = transactions.document()
                stxRef.set(
                    WalletTransaction(
                        id = stxRef.id,
                        userId = sellerId,
                        type = "RECEIVE",
                        amount = amount,
                        note = "Nhận thanh toán đơn hàng",
                        createdAt = System.currentTimeMillis()
                    )
                ).await()

                // Đồng bộ walletBalance vào User document của seller
                db.collection(AppConstant.COLLECTION_USERS).document(sellerId)
                    .update("walletBalance", com.google.firebase.firestore.FieldValue.increment(amount))
                    .await()
            }

            // Đồng bộ buyer
            db.collection(AppConstant.COLLECTION_USERS).document(userId)
                .update("walletBalance", newBuyerBalance).await()

            Result.success(newBuyerBalance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Lấy lịch sử giao dịch gần nhất */
    suspend fun getTransactions(userId: String, limit: Long = 20): Result<List<WalletTransaction>> {
        return try {
            val snapshot = transactions
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            Result.success(snapshot.toObjects(WalletTransaction::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
