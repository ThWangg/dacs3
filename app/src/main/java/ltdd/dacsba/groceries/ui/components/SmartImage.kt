package ltdd.dacsba.groceries.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Composable hiển thị ảnh từ:
 * - URL (https://...) → dùng Coil bình thường
 * - Base64 (data:image/jpeg;base64,...) → decode thủ công rồi hiển thị
 */
@Composable
fun SmartImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model.startsWith("data:image")) {
        // Base64 image
        val bitmap = remember(model) {
            try {
                val base64Part = model.substringAfter("base64,")
                val bytes = Base64.decode(base64Part, Base64.NO_WRAP)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    } else {
        // URL bình thường
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
