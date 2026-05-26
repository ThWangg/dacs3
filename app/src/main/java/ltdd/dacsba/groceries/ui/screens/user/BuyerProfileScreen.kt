package ltdd.dacsba.groceries.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ltdd.dacsba.groceries.ui.components.SmartImage
import ltdd.dacsba.groceries.ui.screens.user.AccentOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerProfileScreen(
    navController: NavController,
    viewModel: BuyerHomeViewModel,
    onLogout: () -> Unit,
    onSwitchToSeller: () -> Unit
) {
    val currentUser by viewModel.currentUser
    val isUploadingAvatar by viewModel.isUploadingAvatar
    val requestStatus by viewModel.sellerRequestStatus
    val isSubmitting by viewModel.isSubmittingRequest
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSellerSheet by remember { mutableStateOf(false) }
    var requestMessage by remember { mutableStateOf("") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }

    val profileMessage by viewModel.profileMessage
    LaunchedEffect(profileMessage) {
        profileMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearProfileMessage()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            localAvatarUri = it
        }
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp)) },
            title = { Text("Đăng xuất?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Đăng xuất", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Huỷ") }
            }
        )
    }

    if (showSellerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSellerSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE0E0E0))
                        .align(Alignment.CenterHorizontally)
                )
                Text("🏪 Đăng ký trở thành Seller", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(
                    "Sau khi gửi yêu cầu, Admin sẽ xem xét và phê duyệt. Bạn sẽ được chuyển sang tài khoản Seller khi được chấp thuận.",
                    fontSize = 13.sp, color = Color.Gray, lineHeight = 20.sp
                )
                OutlinedTextField(
                    value = requestMessage,
                    onValueChange = { requestMessage = it },
                    label = { Text("Lý do / Giới thiệu bản thân") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                Button(
                    onClick = { viewModel.submitSellerRequest(requestMessage); showSellerSheet = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Store, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gửi yêu cầu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                TextButton(onClick = { showSellerSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Huỷ", color = Color.Gray)
                }
            }
        }
    }

    if (showEditProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditProfileSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE0E0E0))
                        .align(Alignment.CenterHorizontally)
                )
                Text("👤 Chỉnh sửa Profile", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                
                OutlinedTextField(
                    value = editUsername,
                    onValueChange = { editUsername = it },
                    label = { Text("Tên hiển thị") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                
                Button(
                    onClick = { 
                        if (editUsername.isNotBlank()) {
                            viewModel.updateUsername(editUsername)
                            showEditProfileSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Lưu thay đổi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                TextButton(onClick = { showEditProfileSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Huỷ", color = Color.Gray)
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(36.dp)) },
            title = { Text("Về ứng dụng", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Groceries App", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AccentOrange)
                    Text("Phiên bản 1.0.0", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ứng dụng mua sắm hàng tiêu dùng, nông sản sạch trực tuyến với đầy đủ tính năng dành cho Buyer và Seller.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đóng", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF787FF6), Color(0xFF1CA7EC), Color(0xFF1F2F98))))
                .padding(top = 40.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { openGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarData = localAvatarUri ?: currentUser?.avatarUrl?.takeIf { it.isNotBlank() }
                    if (avatarData != null) {
                        val model = if (avatarData is Uri) avatarData.toString() else avatarData as String
                        if (model.startsWith("data:image")) {
                            SmartImage(
                                model = model,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(avatarData).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Text(
                            text = (currentUser?.username?.firstOrNull()?.uppercase() ?: "?"),
                            fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = AccentOrange
                        )
                    }
                    if (isUploadingAvatar) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .offset(x = 35.dp, y = (-20).dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                }

                Text(currentUser?.username ?: "Người dùng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.offset(y = (-12).dp))
                Text(currentUser?.email ?: "", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.offset(y = (-12).dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (localAvatarUri != null) {
                Button(
                    onClick = { 
                        viewModel.uploadAndUpdateAvatar(localAvatarUri!!)
                        localAvatarUri = null
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    enabled = !isUploadingAvatar
                ) {
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xác nhận đổi ảnh", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = { openGallery() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    enabled = !isUploadingAvatar
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if ((currentUser?.avatarUrl ?: "").isNotBlank()) "Đổi ảnh đại diện" else "Thêm ảnh đại diện", fontWeight = FontWeight.SemiBold)
                }
            }
            
            if (currentUser?.avatarUrl?.isNotBlank() == true || localAvatarUri != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        localAvatarUri = null
                        viewModel.removeAvatar()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(0.4f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Xóa ảnh đại diện")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (currentUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Thông tin tài khoản", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    BuyerProfileRow(icon = Icons.Default.Person, label = "Tên hiển thị", value = currentUser!!.username.ifBlank { "Chưa đặt tên" })
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    BuyerProfileRow(icon = Icons.Default.Email, label = "Email", value = currentUser!!.email)
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    BuyerProfileRow(
                        icon = Icons.Default.Badge, 
                        label = "Vai trò", 
                        value = when (currentUser!!.role) {
                            "SELLER" -> "🏪 Seller"
                            "ADMIN"  -> "🔑 Admin"
                            else     -> "🛒 Buyer"
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    iconColor = Color(0xFF4CAF50),
                    bgColor = Color(0xFFE8F5E9),
                    label = "Chỉnh sửa Profile",
                    onClick = {
                        editUsername = currentUser?.username ?: ""
                        showEditProfileSheet = true
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF1976D2),
                    bgColor = Color(0xFFE3F2FD),
                    label = "Về ứng dụng",
                    onClick = {
                        showAboutDialog = true
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Trạng thái yêu cầu Seller
        when (requestStatus) {
            "PENDING"  -> StatusBanner("⏳ Yêu cầu trở thành Seller đang chờ Admin xét duyệt...", Color(0xFFFFF3E0), Color(0xFFE65100))
            "APPROVED" -> StatusBanner("✅ Yêu cầu đã được duyệt! Đăng xuất rồi đăng nhập lại để dùng tài khoản Seller.", Color(0xFFE8F5E9), Color(0xFF2E7D32))
            "REJECTED" -> {
                StatusBanner("❌ Yêu cầu bị từ chối. Bấm nút dưới để gửi lại.", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                Spacer(Modifier.height(8.dp))
            }
        }

        if (currentUser?.role == "BUYER" && requestStatus != "PENDING" && requestStatus != "APPROVED") {
            OutlinedButton(
                onClick = { showSellerSheet = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange)
            ) {
                Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Đăng ký làm Người Bán (Seller)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(12.dp))

        if (currentUser?.role == "SELLER") {
            OutlinedButton(
                onClick = onSwitchToSeller,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF388E3C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF388E3C))
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chuyển sang Giao diện Bán Hàng", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun BuyerProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    bgColor: Color,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
