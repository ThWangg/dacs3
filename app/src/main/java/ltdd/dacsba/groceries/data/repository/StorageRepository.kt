package ltdd.dacsba.groceries.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

    /**
     * Upload ảnh từ Uri (được chọn từ Gallery) lên Firebase Storage.
     * @param uri  Uri của ảnh trên thiết bị (từ ActivityResultContracts.GetContent)
     * @param path Đường dẫn lưu trong Storage, ví dụ: "products/abc123.jpg"
     * @return URL download của ảnh sau khi upload thành công
     */
    suspend fun uploadImage(uri: Uri, path: String): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload từ ByteArray — đáng tin cậy hơn putFile() với content:// URI từ Gallery.
     * Dùng khi putFile() báo lỗi "Object does not exist at location".
     */
    suspend fun uploadImageBytes(bytes: ByteArray, path: String): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa ảnh khỏi Firebase Storage theo URL download.
     */
    suspend fun deleteImageByUrl(downloadUrl: String): Result<Boolean> {
        return try {
            if (downloadUrl.isBlank()) return Result.success(true)
            val ref = storage.getReferenceFromUrl(downloadUrl)
            ref.delete().await()
            Result.success(true)
        } catch (e: Exception) {
            // Ảnh có thể đã bị xóa trước, bỏ qua lỗi
            Result.success(true)
        }
    }
}
