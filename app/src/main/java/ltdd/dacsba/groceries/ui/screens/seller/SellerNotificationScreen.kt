package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.Product
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerNotificationScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val products by viewModel.products
    val orders by viewModel.orders

    val notifications = remember(products, orders) {
        val list = mutableListOf<NotificationItem>()

        products.filter { it.status == "APPROVED" }.forEach { p ->
            list.add(
                NotificationItem(
                    id = p.id,
                    title = "Sản phẩm được duyệt",
                    message = "Admin đã duyệt sản phẩm \"${p.name}\". Sản phẩm hiện đã có trên shop.",
                    timestamp = p.createdAt,
                    type = "PRODUCT_APPROVED"
                )
            )
        }

        orders.forEach { o ->
            list.add(
                NotificationItem(
                    id = o.orderId,
                    title = "Đơn hàng mới",
                    message = "Bạn có một đơn hàng mới từ ${o.buyerName}. Tổng tiền: ${o.totalAmount}đ",
                    timestamp = o.createdAt,
                    type = "NEW_ORDER"
                )
            )
        }
        list.sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Không có thông báo nào", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id + it.type }) { notif ->
                    NotificationCard(notif)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = if (notif.type == "PRODUCT_APPROVED") Icons.Default.CheckCircle else Icons.Default.LocalShipping
            val tint = if (notif.type == "PRODUCT_APPROVED") Color(0xFF1565C0) else Color(0xFFE65100)
            val bg = if (notif.type == "PRODUCT_APPROVED") Color(0xFFE3F2FD) else Color(0xFFFFF3E0)

            Surface(shape = CircleShape, color = bg, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(notif.message, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notif.timestamp)),
                    fontSize = 11.sp, color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
