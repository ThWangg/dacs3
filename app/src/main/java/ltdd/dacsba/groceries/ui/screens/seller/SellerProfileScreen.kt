package ltdd.dacsba.groceries.ui.screens.seller

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ltdd.dacsba.groceries.ui.components.AppTextField
import ltdd.dacsba.groceries.ui.components.SmartImage

val SellerGreen = Color(0xFF1CA7EC)
val SellerGreenLight = Color(0xFF787FF6)
val SellerBg = Color(0xFFFBFBFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    navController: NavController,
    viewModel: SellerProfileViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onSwitchToBuyer: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading
    val isEditMode by viewModel.isEditMode

    val snackbarHostState = remember { SnackbarHostState() }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uiState.updateMessage) {
        uiState.updateMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUpdateMessage()
        }
    }

    // Gallery launcher
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

    if (showProfileSheet) {
        SellerProfileBottomSheet(
            username = uiState.username,
            email = uiState.email,
            shopName = uiState.shopName,
            phone = uiState.phone,
            avatarUrl = uiState.avatarUrl,
            localAvatarUri = localAvatarUri,
            isEditMode = isEditMode,
            isLoading = isLoading,
            isUploading = uiState.isUploadingAvatar,
            onUsernameChange = { viewModel.onUsernameChange(it) },
            onShopNameChange = { viewModel.onShopNameChange(it) },
            onPhoneChange = { viewModel.onPhoneChange(it) },
            onEnterEdit = { viewModel.enterEditMode() },
            onSave = { viewModel.saveProfile() },
            onCancelEdit = { viewModel.cancelEdit() },
            onPickAvatar = { openGallery() },
            onConfirmAvatar = { uri -> 
                viewModel.uploadAvatar(uri)
                localAvatarUri = null
            },
            onRemoveAvatar = {
                localAvatarUri = null
                viewModel.removeAvatar()
            },
            onLogout = { showProfileSheet = false; showLogoutDialog = true },
            onDismiss = { viewModel.cancelEdit(); showProfileSheet = false; localAvatarUri = null }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp)) },
            title = { Text("Đăng xuất?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản Seller không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        SellerProfileContent(
            username = uiState.username,
            email = uiState.email,
            shopName = uiState.shopName,
            phone = uiState.phone,
            avatarUrl = uiState.avatarUrl,
            localAvatarUri = localAvatarUri,
            isUploading = uiState.isUploadingAvatar,
            onAvatarClick = { showProfileSheet = true },
            onLogoutClick = { showLogoutDialog = true },
            onSwitchToBuyer = onSwitchToBuyer,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun SellerProfileContent(
    username: String,
    email: String,
    shopName: String,
    phone: String,
    avatarUrl: String = "",
    localAvatarUri: Uri? = null,
    isUploading: Boolean = false,
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSwitchToBuyer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SellerBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(SellerGreen, SellerGreenLight)))
                .padding(top = 32.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar bấm được
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarData = localAvatarUri ?: avatarUrl.takeIf { it.isNotBlank() }
                    if (avatarData != null) {
                        val model = if (avatarData is Uri) avatarData.toString()
                        else avatarData as String
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
                            text = (username.firstOrNull() ?: 'S').uppercaseChar().toString(),
                            fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = SellerGreen
                        )
                    }
                    // Loading overlay
                    if (isUploading) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    }
                }

                // Camera icon badge
                Box(
                    modifier = Modifier
                        .offset(x = 30.dp, y = (-20).dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SellerGreen, modifier = Modifier.size(16.dp))
                }

                Text(username.ifBlank { "Seller" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.offset(y = (-12).dp))
                Text(email, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.offset(y = (-12).dp))
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Text("SELLER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
                }
            }
        }

        Spacer(Modifier.height((-16).dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Thông tin tài khoản", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                ProfileDetailRow(Icons.Default.Store, "Tên Shop", shopName.ifBlank { "Chưa thiết lập" })
                HorizontalDivider(color = Color(0xFFF0F0F0))
                ProfileDetailRow(Icons.Default.Person, "Chủ cửa hàng", username.ifBlank { "Chưa đặt tên" })
                HorizontalDivider(color = Color(0xFFF0F0F0))
                ProfileDetailRow(Icons.Default.Phone, "Số điện thoại", phone.ifBlank { "Chưa thiết lập" })
                HorizontalDivider(color = Color(0xFFF0F0F0))
                ProfileDetailRow(Icons.Default.Email, "Email", email)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Settings card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingsRow(icon = Icons.Default.Edit, label = "Chỉnh sửa Profile", iconColor = SellerGreen, onClick = onAvatarClick)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                SettingsRow(icon = Icons.Default.Info, label = "Về ứng dụng", iconColor = Color(0xFF1565C0), onClick = {})
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onSwitchToBuyer,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF57C00)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57C00))
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Chuyển sang Giao diện Mua Hàng", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout button
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SellerGreen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 15.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileBottomSheet(
    username: String,
    email: String,
    shopName: String,
    phone: String,
    avatarUrl: String,
    localAvatarUri: Uri?,
    isEditMode: Boolean,
    isLoading: Boolean,
    isUploading: Boolean,
    onUsernameChange: (String) -> Unit,
    onShopNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEnterEdit: () -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    onPickAvatar: () -> Unit,
    onConfirmAvatar: (Uri) -> Unit,
    onRemoveAvatar: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Handle bar
            Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0)))

            Text("👤 Hồ sơ Seller", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

            // Avatar lớn — bấm để đổi
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SellerGreen)
                        .clickable { onPickAvatar() },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarData = localAvatarUri ?: avatarUrl.takeIf { it.isNotBlank() }
                    if (avatarData != null) {
                        val model = if (avatarData is Uri) avatarData.toString()
                        else avatarData as String
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
                            (username.firstOrNull() ?: 'S').uppercaseChar().toString(),
                            fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                    }
                    if (isUploading) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                // Camera badge
                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = SellerGreenLight, shadowElevation = 4.dp) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text("Bấm vào ảnh để thay đổi", fontSize = 12.sp, color = Color.Gray)

            if (!isEditMode) {
                Text(username, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(email, color = Color.Gray, fontSize = 13.sp)

                // 1. Đổi ảnh
                if (localAvatarUri != null) {
                    Button(
                        onClick = { onConfirmAvatar(localAvatarUri) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Xác nhận đổi ảnh", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Button(
                        onClick = onPickAvatar,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SellerGreen),
                        enabled = !isUploading
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (avatarUrl.isNotBlank()) "Đổi ảnh đại diện" else "Thêm ảnh đại diện",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 2. Chỉnh sửa thông tin
                Button(
                    onClick = onEnterEdit,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chỉnh sửa thông tin", fontWeight = FontWeight.SemiBold)
                }

                // 3. Xóa ảnh (chỉ hiện nếu có avatar)
                if (avatarUrl.isNotBlank() || localAvatarUri != null) {
                    OutlinedButton(
                        onClick = onRemoveAvatar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(0.4f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa ảnh đại diện")
                    }
                }

            } else {
                // Edit mode
                Text("Chỉnh sửa Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                AppTextField(value = username, onValueChange = onUsernameChange, label = "Tên hiển thị", modifier = Modifier.fillMaxWidth())
                AppTextField(value = shopName, onValueChange = onShopNameChange, label = "Tên Shop", modifier = Modifier.fillMaxWidth())
                AppTextField(value = phone, onValueChange = onPhoneChange, label = "Số điện thoại", modifier = Modifier.fillMaxWidth())
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SellerGreen)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Lưu thay đổi", fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCancelEdit, modifier = Modifier.fillMaxWidth()) { Text("Huỷ", color = Color.Gray) }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerProfilePreview() {
    SellerProfileContent(
        username = "Seller TAUT",
        email = "seller@tautshop.com",
        shopName = "Taub Shop",
        phone = "0987654321",
        onAvatarClick = {},
        onLogoutClick = {},
        onSwitchToBuyer = {}
    )
}
