package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

val AdminGreen = Color(0xFF2E7D32)
val AdminGreenLight = Color(0xFF7CB342)
val AdminBg = Color(0xFFF4F6F8)

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val users by viewModel.users
    val products by viewModel.products
    val isLoading by viewModel.isLoading

    AdminDashboardContent(
        totalUsers = viewModel.totalUsers,
        totalProducts = viewModel.totalProducts,
        totalBuyers = viewModel.totalBuyers,
        totalSellers = viewModel.totalSellers,
        pendingRequests = viewModel.pendingRequests.value.size,
        isLoading = isLoading,
        onRefresh = { viewModel.loadAll(); viewModel.loadPendingRequests() }
    )
}

@Composable
fun AdminDashboardContent(
    totalUsers: Int,
    totalProducts: Int,
    totalBuyers: Int,
    totalSellers: Int,
    pendingRequests: Int = 0,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header gradient banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(AdminGreen, AdminGreenLight))
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    "Admin Dashboard",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    "Tổng quan hệ thống TAUT Shop",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AdminGreenLight
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Thống kê",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        // Row 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Person,
                label = "Người dùng",
                value = totalUsers.toString(),
                containerColor = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1565C0)
            )
            AdminStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ShoppingCart,
                label = "Sản phẩm",
                value = totalProducts.toString(),
                containerColor = Color(0xFFE8F5E9),
                iconColor = AdminGreen
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AccountCircle,
                label = "Buyers",
                value = totalBuyers.toString(),
                containerColor = Color(0xFFFFF3E0),
                iconColor = Color(0xFFE65100)
            )
            AdminStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Store,
                label = "Sellers",
                value = totalSellers.toString(),
                containerColor = Color(0xFFF3E5F5),
                iconColor = Color(0xFF7B1FA2)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 3 — Yêu cầu Seller đang chờ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Notifications,
                label = "Yêu cầu chờ",
                value = pendingRequests.toString(),
                containerColor = if (pendingRequests > 0) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                iconColor = if (pendingRequests > 0) Color(0xFFD32F2F) else Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))

        // Quick info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Thông tin nhanh",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                AdminInfoRow(Icons.Default.CheckCircle, "Trạng thái hệ thống", "Hoạt động", Color(0xFF2E7D32))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0))
                AdminInfoRow(Icons.Default.Star, "Phiên bản app", "v1.0.0", Color(0xFFE65100))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0))
                AdminInfoRow(Icons.Default.Security, "Bảo mật", "Firebase Auth", Color(0xFF1565C0))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = iconColor)
            Text(label, fontSize = 13.sp, color = iconColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun AdminInfoRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.DarkGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardPreview() {
    AdminDashboardContent(
        totalUsers = 24,
        totalProducts = 58,
        totalBuyers = 20,
        totalSellers = 4,
        isLoading = false,
        onRefresh = {}
    )
}
