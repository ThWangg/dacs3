package ltdd.dacsba.groceries.ui.screens.admin

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ltdd.dacsba.groceries.ui.components.AppTextField
import ltdd.dacsba.groceries.ui.components.SmartImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val adminEmail by viewModel.adminEmail
    val adminUsername by viewModel.adminUsername
    val adminAvatarUrl by viewModel.adminAvatarUrl
    val isLoading by viewModel.isLoading
    val isUploading by viewModel.isUploading
    val isEditingProfile by viewModel.isEditingProfile
    val editUsername by viewModel.editUsername
    val snack by viewModel.snackMessage
    val snackbarHostState = remember { SnackbarHostState() }

    var showProfileSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    LaunchedEffect(snack) {
        snack?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnack() }
    }

    // Gallery launcher — GetContent() tự cấp quyền tạm thời, không cần xin quyền thủ công
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Lấy persistent read permission để URI không bị revoke khi đọc bytes
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* Một số URI không hỗ trợ persistent, bỏ qua */ }
            localAvatarUri = it
            viewModel.uploadAdminAvatar(it)
        }
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    // Profile bottom sheet
    if (showProfileSheet) {
        AdminProfileBottomSheet(
            username = adminUsername,
            email = adminEmail,
            avatarUrl = adminAvatarUrl,
            localAvatarUri = localAvatarUri,
            isEditMode = isEditingProfile,
            editUsername = editUsername,
            isLoading = isLoading,
            isUploading = isUploading,
            onEditUsername = { viewModel.editUsername.value = it },
            onEnterEdit = { viewModel.enterEditProfile() },
            onSave = { viewModel.saveProfile() },
            onCancelEdit = { viewModel.cancelEditProfile() },
            onPickAvatar = { openGallery() },
            onRemoveAvatar = {
                localAvatarUri = null
                viewModel.removeAdminAvatar()
            },
            onLogout = { showProfileSheet = false; showLogoutDialog = true },
            onDismiss = { viewModel.cancelEditProfile(); showProfileSheet = false; localAvatarUri = null }
        )
    }

    // Logout confirm dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp)) },
            title = { Text("Đăng xuất?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản Admin không?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() },
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
        AdminProfileContent(
            adminUsername = adminUsername,
            adminEmail = adminEmail,
            adminAvatarUrl = adminAvatarUrl,
            localAvatarUri = localAvatarUri,
            isUploading = isUploading,
            onAvatarClick = { showProfileSheet = true },
            onLogoutClick = { showLogoutDialog = true },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun AdminProfileContent(
    adminUsername: String,
    adminEmail: String,
    adminAvatarUrl: String = "",
    localAvatarUri: Uri? = null,
    isUploading: Boolean = false,
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AdminBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(AdminGreen, AdminGreenLight)))
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
                    val avatarData = localAvatarUri ?: adminAvatarUrl.takeIf { it.isNotBlank() }
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
                            text = (adminUsername.firstOrNull() ?: 'A').uppercaseChar().toString(),
                            fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = AdminGreen
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
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AdminGreen, modifier = Modifier.size(16.dp))
                }

                Text(adminUsername, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.offset(y = (-12).dp))
                Text(adminEmail, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.offset(y = (-12).dp))
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Text("ADMIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp,
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
                ProfileDetailRow(Icons.Default.Person, "Tên hiển thị", adminUsername)
                HorizontalDivider(color = Color(0xFFF0F0F0))
                ProfileDetailRow(Icons.Default.Email, "Email", adminEmail)
                HorizontalDivider(color = Color(0xFFF0F0F0))
                ProfileDetailRow(Icons.Default.Shield, "Quyền hạn", "Administrator")
                HorizontalDivider(color = Color(0xFFF0F0F0))
                ProfileDetailRow(Icons.Default.Security, "Xác thực", "Firebase Auth")
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
                SettingsRow(icon = Icons.Default.Edit, label = "Chỉnh sửa Profile", iconColor = AdminGreen, onClick = onAvatarClick)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                SettingsRow(icon = Icons.Default.Info, label = "Về ứng dụng", iconColor = Color(0xFF1565C0), onClick = {})
            }
        }

        Spacer(Modifier.height(16.dp))

        // Logout button
        OutlinedButton(
            onClick = onLogoutClick,
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
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AdminGreen, modifier = Modifier.size(20.dp))
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
fun AdminProfileBottomSheet(
    username: String,
    email: String,
    avatarUrl: String,
    localAvatarUri: Uri?,
    isEditMode: Boolean,
    editUsername: String,
    isLoading: Boolean,
    isUploading: Boolean,
    onEditUsername: (String) -> Unit,
    onEnterEdit: () -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    onPickAvatar: () -> Unit,
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

            Text("👤 Hồ sơ Admin", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

            // Avatar lớn — bấm để đổi
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AdminGreen)
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
                            (username.firstOrNull() ?: 'A').uppercaseChar().toString(),
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
                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = AdminGreenLight, shadowElevation = 4.dp) {
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
                Button(
                    onClick = onPickAvatar,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                    enabled = !isUploading
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (avatarUrl.isNotBlank() || localAvatarUri != null) "Đổi ảnh đại diện" else "Thêm ảnh đại diện",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 2. Chỉnh sửa tên
                Button(
                    onClick = onEnterEdit,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chỉnh sửa tên", fontWeight = FontWeight.SemiBold)
                }

                // 3. Xóa ảnh (chỉ hiện nếu có avatar)
                if (avatarUrl.isNotBlank() || localAvatarUri != null) {
                    OutlinedButton(
                        onClick = onRemoveAvatar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(0.4f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa ảnh đại diện")
                    }
                }

            } else {
                // Edit mode
                Text("Chỉnh sửa Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                AppTextField(value = editUsername, onValueChange = onEditUsername, label = "Tên hiển thị", modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)
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
fun AdminProfilePreview() {
    AdminProfileContent(
        adminUsername = "Admin TAUT",
        adminEmail = "admin@tautshop.com",
        onAvatarClick = {},
        onLogoutClick = {}
    )
}
