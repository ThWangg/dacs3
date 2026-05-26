package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.OrderItem
import ltdd.dacsba.groceries.data.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Tab data class
private data class OrderTab(
    val label: String,
    val status: OrderStatus?,
    val icon: ImageVector
)

private val orderTabs = listOf(
    OrderTab("Chờ duyệt", OrderStatus.PENDING, Icons.Default.Timer),
    OrderTab("Đang giao", OrderStatus.SHIPPING, Icons.Default.LocalShipping),
    OrderTab("Đã giao", OrderStatus.DELIVERED, Icons.Default.CheckCircle),
    OrderTab("Đã hủy", OrderStatus.CANCELLED, Icons.Default.Close)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOrderScreen(
    navController: NavController,
    viewModel: BuyerOrderViewModel = viewModel()
) {
    val orders by viewModel.orders
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var selectedTabIndex by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Reload on enter
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    val currentTab = orderTabs[selectedTabIndex]
    val filteredOrders = orders.filter { it.status == currentTab.status }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF787FF6), Color(0xFF1CA7EC), Color(0xFF1F2F98))
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Đơn hàng của tôi",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            BuyerOrderTabRow(
                tabs = orderTabs,
                selectedIndex = selectedTabIndex,
                orderCounts = orderTabs.map { tab ->
                    orders.count { it.status == tab.status }
                },
                onTabSelected = { selectedTabIndex = it }
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = AccentOrange,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("Đang tải đơn hàng...", color = Color.Gray)
                            }
                        }
                    }
                    errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = errorMessage ?: "Đã có lỗi xảy ra",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadOrders() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    }
                    filteredOrders.isEmpty() -> {
                        BuyerOrderEmptyState(tab = currentTab)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredOrders, key = { it.orderId }) { order ->
                                BuyerOrderCard(
                                    order = order,
                                    onCancelOrder = if (order.status == OrderStatus.PENDING) {
                                        {
                                            viewModel.cancelOrder(
                                                orderId = order.orderId,
                                                onSuccess = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Đã hủy đơn hàng thành công")
                                                    }
                                                },
                                                onError = { err ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(err)
                                                    }
                                                }
                                            )
                                        }
                                    } else null
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerOrderTabRow(
    tabs: List<OrderTab>,
    selectedIndex: Int,
    orderCounts: List<Int>,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val tabColor by animateColorAsState(
                    targetValue = if (isSelected) AccentOrange else Color.Gray,
                    animationSpec = tween(200),
                    label = "tabColor"
                )
                val tabBgColor by animateColorAsState(
                    targetValue = if (isSelected) AccentOrange.copy(alpha = 0.1f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "tabBg"
                )
                val statusIcon = when (tab.status) {
                    OrderStatus.PENDING -> Icons.Default.Timer
                    OrderStatus.SHIPPING -> Icons.Default.LocalShipping
                    OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                    OrderStatus.CANCELLED -> Icons.Default.Close
                    else -> Icons.Default.List
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tabBgColor)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        IconButton(
                            onClick = { onTabSelected(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = tab.label,
                                tint = tabColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (orderCounts[index] > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .background(AccentOrange, CircleShape)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (orderCounts[index] > 9) "9+" else "${orderCounts[index]}",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        color = tabColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                    if (isSelected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(AccentOrange, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerOrderEmptyState(tab: OrderTab) {
    val (icon, message, subMessage) = when (tab.status) {
        OrderStatus.PENDING -> Triple(
            Icons.Default.Timer,
            "Chưa có đơn chờ duyệt",
            "Các đơn hàng mới đặt sẽ xuất hiện ở đây"
        )
        OrderStatus.SHIPPING -> Triple(
            Icons.Default.LocalShipping,
            "Chưa có đơn đang giao",
            "Đơn hàng đã được xác nhận sẽ xuất hiện ở đây"
        )
        OrderStatus.DELIVERED -> Triple(
            Icons.Default.CheckCircle,
            "Chưa có đơn đã giao",
            "Đơn hàng đã giao thành công sẽ xuất hiện ở đây"
        )
        OrderStatus.CANCELLED -> Triple(
            Icons.Default.Close,
            "Chưa có đơn đã hủy",
            "Đơn hàng bị hủy sẽ xuất hiện ở đây"
        )
        else -> Triple(Icons.Default.List, "Không có đơn hàng", "")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFFF0F0F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF757575)
            )
            if (subMessage.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subMessage,
                    fontSize = 13.sp,
                    color = Color(0xFFBDBDBD)
                )
            }
        }
    }
}

@Composable
private fun BuyerOrderCard(
    order: Order,
    onCancelOrder: (() -> Unit)?
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val (statusColor, statusBg) = when (order.status) {
        OrderStatus.PENDING -> Pair(Color(0xFFF59E0B), Color(0xFFFFF8E1))
        OrderStatus.CONFIRMED -> Pair(Color(0xFF3B82F6), Color(0xFFEFF6FF))
        OrderStatus.SHIPPING -> Pair(Color(0xFF8B5CF6), Color(0xFFF5F3FF))
        OrderStatus.DELIVERED -> Pair(Color(0xFF22C55E), Color(0xFFF0FDF4))
        OrderStatus.CANCELLED -> Pair(Color(0xFFEF4444), Color(0xFFFEF2F2))
    }

    val statusIcon = when (order.status) {
        OrderStatus.PENDING -> Icons.Default.Timer
        OrderStatus.CONFIRMED -> Icons.Default.Info
        OrderStatus.SHIPPING -> Icons.Default.LocalShipping
        OrderStatus.DELIVERED -> Icons.Default.CheckCircle
        OrderStatus.CANCELLED -> Icons.Default.Close
    }

    val dateStr = remember(order.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(statusBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "#${order.orderId.takeLast(8).uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DarkNavy
                        )
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = order.status.displayName,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))

            // Product preview (first 2 items, then expand)
            val previewItems = if (isExpanded) order.items else order.items.take(2)

            previewItems.forEach { item ->
                BuyerOrderItemRow(item = item)
                Spacer(Modifier.height(6.dp))
            }

            if (order.items.size > 2) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Thu gọn ▲" else "Xem thêm ${order.items.size - 2} sản phẩm ▼",
                        color = AccentOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(10.dp))

            // Total & shipping address
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (order.shippingAddress.isNotBlank()) {
                        Text(
                            text = "📍 ${order.shippingAddress}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${order.items.size} sản phẩm",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatBuyerCurrency(order.totalAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentOrange
                    )
                }
            }

            // Cancel button (only for PENDING)
            if (onCancelOrder != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hủy đơn hàng", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text(
                    text = "Xác nhận hủy đơn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkNavy
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc muốn hủy đơn hàng #${order.orderId.takeLast(8).uppercase()} không? Hành động này không thể hoàn tác.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelOrder?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hủy đơn", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Không")
                }
            }
        )
    }
}

@Composable
private fun BuyerOrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(AccentOrange.copy(alpha = 0.5f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${item.productName} x${item.quantity} ${item.unit}",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = formatBuyerCurrency(item.priceAtOrder * item.quantity),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentOrange
        )
    }
}

private fun formatBuyerCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}
