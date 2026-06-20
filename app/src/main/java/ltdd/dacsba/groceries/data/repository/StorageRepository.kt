package ltdd.dacsba.groceries.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

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

suspend fun deleteImageByUrl(downloadUrl: String): Result<Boolean> {
        return try {
            if (downloadUrl.isBlank()) return Result.success(true)
            val ref = storage.getReferenceFromUrl(downloadUrl)
            ref.delete().await()
            Result.success(true)
        } catch (e: Exception) {

            Result.success(true)
        }
    }
}
