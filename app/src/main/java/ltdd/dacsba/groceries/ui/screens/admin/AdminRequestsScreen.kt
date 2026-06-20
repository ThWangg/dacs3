package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.navigation.NavController
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.model.SellerRequest
import ltdd.dacsba.groceries.ui.components.SmartImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminRequestsScreen(navController: NavController, viewModel: AdminViewModel) {
    val requests by viewModel.pendingRequests
    val products by viewModel.pendingProducts
    val isLoading by viewModel.isLoading

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Yêu cầu Seller", "Duyệt Sản phẩm")

    LaunchedEffect(Unit) { viewModel.loadPendingRequests() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBg)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(AdminGreen, AdminGreenLight)))
                .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    "Quản lý Phê duyệt",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    "${requests.size} Seller, ${products.size} Sản phẩm chờ duyệt",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = AdminGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AdminGreen
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) AdminGreen else Color.Gray
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdminGreen)
            }
        } else {
            if (selectedTab == 0) {
                if (requests.isEmpty()) {
                    EmptyState("Không có yêu cầu Seller nào đang chờ")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests, key = { it.requestId }) { req ->
                            RequestCard(
                                request = req,
                                onApprove = { viewModel.approveSellerRequest(req) },
                                onReject = { viewModel.rejectSellerRequest(req.requestId) }
                            )
                        }
                    }
                }
            } else {
                if (products.isEmpty()) {
                    EmptyState("Không có sản phẩm nào cần duyệt")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products, key = { it.id }) { prod ->
                            ProductApprovalCard(
                                product = prod,
                                onApprove = { viewModel.approveProduct(prod) },
                                onReject = { viewModel.rejectProduct(prod) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✅", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RequestCard(
    request: SellerRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showConfirmApprove by remember { mutableStateOf(false) }
    var showConfirmReject by remember { mutableStateOf(false) }

    if (showConfirmApprove) {
        AlertDialog(
            onDismissRequest = { showConfirmApprove = false },
            containerColor = Color.White,
            title = { Text("Duyệt yêu cầu?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn duyệt yêu cầu của ${request.username}?\nTài khoản này sẽ được chuyển thành SELLER.") },
            confirmButton = {
                Button(onClick = { onApprove(); showConfirmApprove = false }, colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)) { Text("✅ Duyệt") }
            },
            dismissButton = { TextButton(onClick = { showConfirmApprove = false }) { Text("Huỷ") } }
        )
    }

    if (showConfirmReject) {
        AlertDialog(
            onDismissRequest = { showConfirmReject = false },
            containerColor = Color.White,
            title = { Text("Từ chối yêu cầu?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn từ chối yêu cầu của ${request.username}?") },
            confirmButton = {
                Button(onClick = { onReject(); showConfirmReject = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("❌ Từ chối") }
            },
            dismissButton = { TextButton(onClick = { showConfirmReject = false }) { Text("Huỷ") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(AdminGreen), contentAlignment = Alignment.Center) {
                    if (request.avatarUrl.isNotBlank()) {
                        SmartImage(model = request.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.username.ifBlank { "Chưa đặt tên" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(request.email, fontSize = 12.sp, color = Color.Gray)
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(request.createdAt)), fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }

            if (request.message.isNotBlank()) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF5F5F5)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("💬 Lý do:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(request.message, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showConfirmReject = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)), border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(0.5f))) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Từ chối", fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = { showConfirmApprove = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Duyệt", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ProductApprovalCard(
    product: Product,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showConfirmApprove by remember { mutableStateOf(false) }
    var showConfirmReject by remember { mutableStateOf(false) }

    if (showConfirmApprove) {
        AlertDialog(
            onDismissRequest = { showConfirmApprove = false },
            containerColor = Color.White,
            title = { Text("Duyệt sản phẩm?", fontWeight = FontWeight.Bold) },
            text = { Text("Cho phép hiển thị sản phẩm \"${product.name}\" lên shop?") },
            confirmButton = {
                Button(onClick = { onApprove(); showConfirmApprove = false }, colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)) { Text("✅ Duyệt") }
            },
            dismissButton = { TextButton(onClick = { showConfirmApprove = false }) { Text("Huỷ") } }
        )
    }

    if (showConfirmReject) {
        AlertDialog(
            onDismissRequest = { showConfirmReject = false },
            containerColor = Color.White,
            title = { Text("Từ chối sản phẩm?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn từ chối và ẩn sản phẩm \"${product.name}\"?") },
            confirmButton = {
                Button(onClick = { onReject(); showConfirmReject = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("❌ Từ chối") }
            },
            dismissButton = { TextButton(onClick = { showConfirmReject = false }) { Text("Huỷ") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                    if (product.imageUrl.isNotBlank()) {
                        SmartImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name.ifBlank { "Sản phẩm không tên" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(product.price)
                    Text("${formattedPrice}đ / ${product.unit}", fontSize = 13.sp, color = AdminGreen, fontWeight = FontWeight.SemiBold)
                    Text("Stock: ${product.stock} | Lượt bán: ${product.soldCount}", fontSize = 11.sp, color = Color.Gray)
                    Text("Đăng lúc: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(product.createdAt)), fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }

            if (product.description.isNotBlank()) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF5F5F5)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("📝 Mô tả:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(product.description, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 3)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showConfirmReject = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)), border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(0.5f))) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Từ chối", fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = { showConfirmApprove = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Duyệt", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
