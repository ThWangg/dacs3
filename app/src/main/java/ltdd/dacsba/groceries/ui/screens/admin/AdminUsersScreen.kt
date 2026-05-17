package ltdd.dacsba.groceries.ui.screens.admin

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.ui.components.ImagePickerButton


@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val users by viewModel.users
    val isLoading by viewModel.isLoading
    val isUploading by viewModel.isUploading

    AdminUsersContent(
        users = users,
        isLoading = isLoading,
        isUploading = isUploading,
        onToggleDeactivate = { user -> viewModel.toggleUserDeactivate(user) },
        onUpdateAvatar = { uid, url -> viewModel.updateUserAvatar(uid, url) },
        onUploadAvatar = { uri, onDone -> viewModel.uploadImage(uri, "avatars", onDone) },
        onRefresh = { viewModel.loadAll() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersContent(
    users: List<User>,
    isLoading: Boolean,
    isUploading: Boolean = false,
    onToggleDeactivate: (User) -> Unit,
    onUpdateAvatar: (String, String) -> Unit,
    onUploadAvatar: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    onRefresh: () -> Unit
) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = users.filter {
        it.username.contains(searchQuery, ignoreCase = true) ||
        it.email.contains(searchQuery, ignoreCase = true)
    }

    // Bottom Sheet chi tiết user (bao gồm quản lý avatar)
    selectedUser?.let { user ->
        UserDetailSheet(
            user = user,
            isUploading = isUploading,
            onDismiss = { selectedUser = null },
            onToggleDeactivate = { onToggleDeactivate(user); selectedUser = null },
            onUpdateAvatar = { url -> onUpdateAvatar(user.uid, url) },
            onUploadAvatar = onUploadAvatar
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(AdminBg)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(AdminGreen, AdminGreenLight)))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text("Quản lý Users", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("${users.size} tài khoản", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
            }
            IconButton(onClick = onRefresh, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Tìm tên, email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminGreenLight,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminGreenLight)

        if (filtered.isEmpty() && !isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Không tìm thấy người dùng", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.uid }) { user ->
                    UserRowCard(user = user, onClick = { selectedUser = user })
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ─── User Row Card ────────────────────────────────────────────────────────────

@Composable
fun UserRowCard(user: User, onClick: () -> Unit) {
    val roleColor = when (user.role) {
        "ADMIN"  -> Color(0xFF7B1FA2)
        "SELLER" -> Color(0xFF1565C0)
        else     -> Color(0xFF2E7D32)
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (user.isDeactivated) Color(0xFFFFF3F3) else Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar: ảnh nếu có URL, chữ cái nếu không
            UserAvatarCircle(user = user, size = 46)

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.username.ifBlank { "Chưa đặt tên" },
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(user.email, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(20.dp), color = roleColor.copy(alpha = 0.12f)) {
                    Text(user.role, color = roleColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                if (user.isDeactivated) {
                    Spacer(Modifier.height(4.dp))
                    Text("Bị khóa", fontSize = 11.sp, color = Color.Red)
                }
                if (user.avatarUrl.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Icon(Icons.Default.Image, contentDescription = null, tint = AdminGreenLight, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ─── Avatar Circle Component ─────────────────────────────────────────────────

@Composable
fun UserAvatarCircle(user: User, size: Int = 46) {
    val roleColor = when (user.role) {
        "ADMIN"  -> Color(0xFF7B1FA2)
        "SELLER" -> Color(0xFF1565C0)
        else     -> Color(0xFF2E7D32)
    }
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (user.avatarUrl.isNotBlank()) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(shape = CircleShape, color = if (user.isDeactivated) Color.LightGray else roleColor, modifier = Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (user.username.firstOrNull() ?: 'U').uppercaseChar().toString(),
                        color = Color.White, fontWeight = FontWeight.Bold,
                        fontSize = (size * 0.4).sp
                    )
                }
            }
        }
    }
}

// ─── User Detail Bottom Sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailSheet(
    user: User,
    isUploading: Boolean = false,
    onDismiss: () -> Unit,
    onToggleDeactivate: () -> Unit,
    onUpdateAvatar: (String) -> Unit,
    onUploadAvatar: (Uri, (String) -> Unit) -> Unit = { _, _ -> }
) {
    val roleColor = when (user.role) {
        "ADMIN"  -> Color(0xFF7B1FA2)
        "SELLER" -> Color(0xFF1565C0)
        else     -> Color(0xFF2E7D32)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAvatarSection by remember { mutableStateOf(false) }
    var avatarUrlInput by remember { mutableStateOf(user.avatarUrl) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle bar
            Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0)))
            Spacer(Modifier.height(16.dp))

            // Avatar lớn + nút edit avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatarCircle(user = user.copy(avatarUrl = if (avatarUrlInput != user.avatarUrl) avatarUrlInput else user.avatarUrl), size = 80)
                IconButton(
                    onClick = { showAvatarSection = !showAvatarSection },
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(AdminGreen)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa avatar", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(user.username.ifBlank { "Chưa đặt tên" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(user.email, color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = roleColor.copy(alpha = 0.12f)) {
                Text(user.role, color = roleColor, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp))
            }

            if (showAvatarSection) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(16.dp))

                Text("🖼️ Ảnh đại diện", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(10.dp))

                // ImagePickerButton hình tròn — chọn ảnh từ Gallery, upload Firebase Storage
                ImagePickerButton(
                    currentImageUrl = avatarUrlInput,
                    isUploading = isUploading,
                    onImagePicked = { uri ->
                        // Hiện preview local ngay, không cần chờ upload xong
                        avatarUrlInput = uri.toString()
                        onUploadAvatar(uri) { downloadUrl ->
                            avatarUrlInput = downloadUrl
                            onUpdateAvatar(downloadUrl)
                        }
                    },
                    onRemoveImage = {
                        avatarUrlInput = ""
                        onUpdateAvatar("")
                        showAvatarSection = false
                    },
                    label = "Chọn ảnh đại diện",
                    previewHeight = 100.dp,
                    accentColor = AdminGreen,
                    isCircle = true,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(16.dp))

            // Thông tin chi tiết
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DetailRow("Email", user.email)
                DetailRow("UID", user.uid.take(16) + "…")
                DetailRow("Trạng thái", if (user.isDeactivated) "Bị khóa 🔒" else "Hoạt động ✅")
                DetailRow("Có avatar", if (user.avatarUrl.isNotBlank()) "Có ✅" else "Chưa có")
            }

            Spacer(Modifier.height(20.dp))

            // Khóa/Mở khóa
            Button(
                onClick = onToggleDeactivate,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.isDeactivated) AdminGreen else Color(0xFFD32F2F)
                )
            ) {
                Icon(
                    if (user.isDeactivated) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null, modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (user.isDeactivated) "Mở khóa tài khoản" else "Khóa tài khoản",
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Đóng", color = Color.Gray)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminUsersPreview() {
    AdminUsersContent(
        users = listOf(
            User(uid = "1", username = "Nguyễn Văn A", email = "a@gmail.com", role = "BUYER"),
            User(uid = "2", username = "Seller B", email = "b@gmail.com", role = "SELLER", avatarUrl = "https://i.pravatar.cc/150?img=3"),
            User(uid = "3", username = "Admin C", email = "c@gmail.com", role = "ADMIN"),
            User(uid = "4", username = "Blocked D", email = "d@gmail.com", role = "BUYER", isDeactivated = true),
        ),
        isLoading = false,
        onToggleDeactivate = {},
        onUpdateAvatar = { _, _ -> },
        onRefresh = {}
    )
}
