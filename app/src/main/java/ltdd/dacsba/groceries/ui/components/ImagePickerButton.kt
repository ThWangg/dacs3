package ltdd.dacsba.groceries.ui.components

import android.Manifest
import android.net.Uri
import android.os.Build
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

/**
 * Nút chọn ảnh từ thư viện (Gallery).
 * - Android 13+: tự xin quyền READ_MEDIA_IMAGES
 * - Android 12-: tự xin quyền READ_EXTERNAL_STORAGE
 * Khi người dùng chọn ảnh, trả về Uri qua [onImagePicked].
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
    accentColor: Color = Color(0xFF2E7D32),
    isCircle: Boolean = false
) {
    // 1. Launcher mở Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagePicked(it) }
    }

    // 2. Launcher xin quyền — sau khi được cấp thì mở Gallery
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) galleryLauncher.launch("image/*")
    }

    // 3. Hàm trung gian: xin quyền phù hợp với phiên bản Android rồi mở Gallery
    fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES       // Android 13+
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE  // Android ≤ 12
        }
        permissionLauncher.launch(permission)
    }

    val context = LocalContext.current

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
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
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
                    // Preview hình chữ nhật (dùng cho sản phẩm)
                    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(previewHeight)
                                .clickable { openGallery() },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
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

                // Nút xóa ảnh góc trên phải (chỉ dành cho hình chữ nhật)
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

            // Nút xóa ảnh dạng text (chỉ dành cho hình tròn/avatar)
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
            // ── Chưa có ảnh: Nút chọn ảnh ──
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
