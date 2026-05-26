package ltdd.dacsba.groceries.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ltdd.dacsba.groceries.ui.components.SmartImage

/**
 * Nút chọn ảnh từ thư viện (Gallery).
 * Dùng GetContent() — launcher này tự cấp quyền đọc tạm thời cho URI được chọn,
 * không cần xin READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE riêng.
 * Gọi takePersistableUriPermission để giữ quyền đọc lâu dài, tránh lỗi
 * "Object does not exist at location" khi đọc bytes upload lên Firebase Storage.
 */
@Composable
fun ImagePickerButton(
    currentImageUrl: String,
    isUploading: Boolean,
    onImagePicked: (Uri) -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Chọn ảnh",
    previewHeight: Dp = 160.dp,
    accentColor: Color = Color(0xFF787FF6),
    isCircle: Boolean = false
) {
    val context = LocalContext.current

    // Launcher mở Gallery — GetContent() tự cấp quyền đọc tạm thời
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Cố lấy persistent read permission để URI không bị revoke khi đọc bytes sau này
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* Một số URI không hỗ trợ persistent, bỏ qua */ }
            onImagePicked(it)
        }
    }

    // Mở Gallery trực tiếp — không cần xin quyền thủ công
    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (currentImageUrl.isNotBlank()) {
            // ── Đã có ảnh: Hiển thị preview + nút đổi/xóa ──
            Box(contentAlignment = Alignment.TopEnd) {
                if (isCircle) {
                    // Preview hình tròn (dùng cho avatar)
                    Box(
                        modifier = Modifier
                            .size(previewHeight)
                            .clip(CircleShape)
                            .clickable { openGallery() },
                        contentAlignment = Alignment.Center
                    ) {
                        val model = if (currentImageUrl.startsWith("data:image")) currentImageUrl 
                            else ImageRequest.Builder(context).data(currentImageUrl).crossfade(true).build()
                        if (currentImageUrl.startsWith("data:image")) {
                            SmartImage(
                                model = currentImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = model,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Text("Đổi ảnh", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                } else {

                    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(previewHeight)
                                .clickable { openGallery() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentImageUrl.startsWith("data:image")) {
                                SmartImage(
                                    model = currentImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(currentImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Bấm để đổi ảnh", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }


                if (!isCircle) {
                    IconButton(
                        onClick = onRemoveImage,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa ảnh", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

             if (isCircle) {
                TextButton(
                    onClick = onRemoveImage,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xóa ảnh đại diện", fontSize = 13.sp)
                }
            }
        } else {
             Box(
                modifier = Modifier
                    .then(
                        if (isCircle) Modifier.size(previewHeight).clip(CircleShape)
                        else Modifier.fillMaxWidth().height(previewHeight).clip(RoundedCornerShape(12.dp))
                    )
                    .border(
                        width = 2.dp,
                        color = accentColor.copy(alpha = 0.4f),
                        shape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)
                    )
                    .background(accentColor.copy(alpha = 0.05f))
                    .clickable { openGallery() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(if (isCircle) 32.dp else 40.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(label, color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Loading indicator khi đang upload lên Firebase Storage
        if (isUploading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accentColor, strokeWidth = 2.dp)
                Text("Đang tải ảnh lên...", fontSize = 12.sp, color = accentColor)
            }
        }
    }
}
