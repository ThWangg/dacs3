package ltdd.dacsba.groceries.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Tiện ích xử lý ảnh: nén + chuyển sang Base64 để lưu thẳng vào Firestore.
 * Không cần Firebase Storage.
 */
object ImageUtils {

    private const val MAX_WIDTH = 600
    private const val MAX_HEIGHT = 600
    private const val JPEG_QUALITY = 60   // 0-100, càng nhỏ file càng nhẹ

    /**
     * Đọc ảnh từ URI, scale nhỏ lại, rồi trả về chuỗi Base64.
     * Kết quả lưu vào Firestore dưới dạng string "data:image/jpeg;base64,..."
     */
    fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Không đọc được file ảnh")

        val original = BitmapFactory.decodeStream(inputStream)
            ?: throw Exception("Không decode được ảnh")

        val scaled = scaleBitmap(original, MAX_WIDTH, MAX_HEIGHT)

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        val bytes = out.toByteArray()

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }

    private fun scaleBitmap(src: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxW && h <= maxH) return src
        val scale = minOf(maxW.toFloat() / w, maxH.toFloat() / h)
        return Bitmap.createScaledBitmap(src, (w * scale).toInt(), (h * scale).toInt(), true)
    }
}
