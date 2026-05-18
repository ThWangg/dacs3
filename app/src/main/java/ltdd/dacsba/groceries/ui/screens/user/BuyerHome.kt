package ltdd.dacsba.groceries.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ltdd.dacsba.groceries.ui.components.SmartImage
import ltdd.dacsba.groceries.R
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.model.User

val DarkNavy = Color(0xFF1B2430)
val AccentOrange = Color(0xFFFF7D4D)

@Composable
fun BuyerHomeScreen(viewModel: BuyerHomeViewModel = viewModel()) {
    val requestResult by viewModel.requestResult
    val profileMessage by viewModel.profileMessage
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(requestResult) {
        requestResult?.let { snackbarHostState.showSnackbar(it); viewModel.clearRequestResult() }
    }
    LaunchedEffect(profileMessage) {
        profileMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearProfileMessage() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            BuyerHomeHeader(viewModel = viewModel)
            BuyerHomeBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerHomeHeader(viewModel: BuyerHomeViewModel = viewModel()) {
    val requestStatus by viewModel.sellerRequestStatus
    val isSubmitting by viewModel.isSubmittingRequest
    val currentUser by viewModel.currentUser
    val isUploadingAvatar by viewModel.isUploadingAvatar
    val context = LocalContext.current

    var showSellerSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var requestMessage by remember { mutableStateOf("") }

    // Preview local khi chọn ảnh
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }

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
            localAvatarUri = it          // preview ngay
            viewModel.uploadAndUpdateAvatar(it)
        }
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    // ── Bottom Sheet Profile ──────────────────────────────────────────────────
    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showProfileSheet = false
                localAvatarUri = null
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE0E0E0))
                )

                Text(
                    "👤 Hồ sơ của tôi",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )

                // ── Avatar lớn — bấm để đổi ──
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFFF7D4D), Color(0xFFFF5722))))
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
                                    model = ImageRequest.Builder(context)
                                        .data(avatarData)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Text(
                                text = (currentUser?.username?.firstOrNull()?.uppercase() ?: "?"),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Lớp mờ khi uploading
                        if (isUploadingAvatar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
                            }
                        }
                    }

                    // Nút camera nhỏ góc dưới phải
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = AccentOrange,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Đổi ảnh",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    "Bấm vào ảnh để thay đổi",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Thông tin user
                if (currentUser != null) {
                    ProfileInfoCard(user = currentUser!!)
                }

                // Nút xóa avatar (chỉ hiện nếu có avatar)
                if ((currentUser?.avatarUrl?.isNotBlank() == true) || localAvatarUri != null) {
                    OutlinedButton(
                        onClick = {
                            localAvatarUri = null
                            viewModel.removeAvatar()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(0.4f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa ảnh đại diện", fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(onClick = { showProfileSheet = false; localAvatarUri = null }) {
                    Text("Đóng", color = Color.Gray)
                }
            }
        }
    }

    // ── Bottom Sheet Seller Request ───────────────────────────────────────────
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

    // ── Header UI ─────────────────────────────────────────────────────────────
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo + Tên
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.tuat_logo),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Taut Shop",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                )
            }

            // Nút bên phải: Avatar + Seller + Search
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {

                // ── Avatar / Profile icon ──
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFF7D4D), Color(0xFFFF5722))))
                        .clickable { showProfileSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = currentUser?.avatarUrl?.takeIf { it.isNotBlank() }
                    if (avatarUrl != null) {
                        if (avatarUrl.startsWith("data:image")) {
                            SmartImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Text(
                            text = (currentUser?.username?.firstOrNull()?.uppercase() ?: "?"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // ── Nút Seller Request ──
                Box {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = when (requestStatus) {
                            "PENDING"  -> Color(0xFFFFF3E0)
                            "APPROVED" -> Color(0xFFE8F5E9)
                            "REJECTED" -> Color(0xFFFFEBEE)
                            else       -> Color(0xFFF7F7F7)
                        },
                        shadowElevation = 2.dp
                    ) {
                        IconButton(onClick = { showSellerSheet = true }) {
                            Icon(
                                imageVector = if (requestStatus != null) Icons.Default.Store else Icons.Default.Notifications,
                                contentDescription = "Đăng ký Seller",
                                modifier = Modifier.size(20.dp),
                                tint = when (requestStatus) {
                                    "PENDING"  -> Color(0xFFE65100)
                                    "APPROVED" -> Color(0xFF2E7D32)
                                    "REJECTED" -> Color(0xFFD32F2F)
                                    else       -> Color.Black
                                }
                            )
                        }
                    }
                    if (requestStatus == "PENDING") {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(Color(0xFFE65100), CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                // ── Search ──
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFFF7F7F7),
                    shadowElevation = 2.dp
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Trạng thái yêu cầu Seller (nếu có)
        when (requestStatus) {
            "PENDING"  -> StatusBanner("⏳ Yêu cầu trở thành Seller đang chờ Admin xét duyệt...", Color(0xFFFFF3E0), Color(0xFFE65100))
            "APPROVED" -> StatusBanner("✅ Yêu cầu đã được duyệt! Đăng xuất rồi đăng nhập lại để dùng tài khoản Seller.", Color(0xFFE8F5E9), Color(0xFF2E7D32))
            "REJECTED" -> StatusBanner("❌ Yêu cầu bị từ chối. Bấm 🔔 để gửi lại.", Color(0xFFFFEBEE), Color(0xFFD32F2F))
        }

        Spacer(modifier = Modifier.height(8.dp))
        CategorySection()
    }
}

/** Card hiển thị thông tin user trong Profile sheet */
@Composable
fun ProfileInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileRow(icon = Icons.Default.Person, label = "Tên", value = user.username.ifBlank { "Chưa đặt tên" })
            HorizontalDivider(color = Color(0xFFEEEEEE))
            ProfileRow(icon = Icons.Default.Email, label = "Email", value = user.email)
            HorizontalDivider(color = Color(0xFFEEEEEE))
            ProfileRow(
                icon = Icons.Default.Badge,
                label = "Vai trò",
                value = when (user.role) {
                    "SELLER" -> "🏪 Seller"
                    "ADMIN"  -> "🔑 Admin"
                    else     -> "🛒 Buyer"
                }
            )
        }
    }
}

@Composable
fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentOrange)
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatusBanner(text: String, bg: Color, textColor: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        shape = RoundedCornerShape(10.dp),
        color = bg
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 12.sp,
            color = textColor,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun CategorySection() {
    val categories = listOf("Fruits", "Fast-food", "Vegetables", "Drinks")
    var selectedCategory by remember { mutableStateOf("Fruits") }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(25.dp)).clickable { selectedCategory = category },
                color = if (isSelected) DarkNavy else Color(0xFFF7F7F7)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = TextStyle(
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BuyerHomeBody() {
    val sampleProducts = listOf(
        Product(id = "1", name = "Apple", price = 10.45, unit = "kg", stock = 55, imageUrl = "https://img.freepik.com/free-photo/red-apples-isolated-white-background_1232-3122.jpg"),
        Product(id = "2", name = "Orange", price = 14.75, unit = "kg", stock = 75, imageUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c4/Orange-Fruit-Pieces.jpg")
    )
    Column(modifier = Modifier.fillMaxSize().padding(top = 20.dp)) {
        Text(
            text = "Popular Fruits",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sampleProducts) { product ->
                ProductCard(product = product, onClick = {})
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SmartImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.name, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text(text = "${product.stock} cal", fontSize = 12.sp, color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$${product.price}/kg", fontWeight = FontWeight.Bold, color = AccentOrange)
                Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = AccentOrange) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BuyerHomeScreenPreview() {
    BuyerHomeScreen()
}