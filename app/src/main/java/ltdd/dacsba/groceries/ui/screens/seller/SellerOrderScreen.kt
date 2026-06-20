package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.Order
import ltdd.dacsba.groceries.data.model.OrderItem
import ltdd.dacsba.groceries.data.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SellerOrderScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val orders by viewModel.orders
    val isLoading by viewModel.isLoading
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        SellerOrderContent(
            orders = orders,
            isLoading = isLoading,
            onUpdateStatus = { orderId, newStatus ->
                viewModel.updateOrderStatus(
                    orderId = orderId,
                    newStatus = newStatus,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Cập nhật trạng thái thành công")
                        }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SellerOrderContent(
    orders: List<Order>,
    isLoading: Boolean,
    onUpdateStatus: (String, OrderStatus) -> Unit = { _, _ -> }
) {
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = if (selectedStatus == null) {
        orders
    } else {
        orders.filter { it.status == selectedStatus }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đơn hàng",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${filteredOrders.size} đơn",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = SellerGreen
            )
        }

OrderStatusFilterRow(
            selectedStatus = selectedStatus,
            onStatusSelected = { status -> selectedStatus = status }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredOrders.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không có đơn hàng",
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders) { order ->
                    OrderItemCard(order = order, onUpdateStatus = onUpdateStatus)
                }
            }
        }
    }
}

@Composable
fun OrderStatusFilterRow(
    selectedStatus: OrderStatus?,
    onStatusSelected: (OrderStatus?) -> Unit
) {
    val statusList = OrderStatus.entries.filter { it != OrderStatus.CONFIRMED }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("Tất cả", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SellerGreen,
                selectedLabelColor = Color.White
            )
        )

        statusList.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(status.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SellerGreen,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun OrderItemCard(order: Order, onUpdateStatus: (String, OrderStatus) -> Unit = { _, _ -> }) {
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFBC02D)
        OrderStatus.CONFIRMED -> Color(0xFF1976D2)
        OrderStatus.SHIPPING -> Color(0xFF7B1FA2)
        OrderStatus.DELIVERED -> Color(0xFF2E7D32)
        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
    }

    val statusBgColor = statusColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "#${order.orderId.takeLast(8).uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = order.buyerName.ifBlank { "Customer XX" },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = order.status.displayName,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.productName} x${item.quantity} ${item.unit}",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatCurrency(item.priceAtOrder * item.quantity),
                        fontSize = 13.sp,
                        color = SellerGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng cộng",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatCurrency(order.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SellerGreen
                )
            }

val nextStatus = when (order.status) {
                OrderStatus.PENDING -> OrderStatus.SHIPPING
                OrderStatus.SHIPPING -> OrderStatus.DELIVERED
                else -> null
            }
            val actionLabel = when (order.status) {
                OrderStatus.PENDING -> "Xác nhận & Giao hàng"
                OrderStatus.SHIPPING -> "Xác nhận đã giao"
                else -> null
            }
            val actionIcon = when (order.status) {
                OrderStatus.PENDING -> Icons.Default.LocalShipping
                OrderStatus.SHIPPING -> Icons.Default.CheckCircle
                else -> null
            }
            val actionColor = when (order.status) {
                OrderStatus.PENDING -> SellerGreen
                OrderStatus.SHIPPING -> Color(0xFF1976D2)
                else -> null
            }

            if (nextStatus != null && actionLabel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                if (order.status == OrderStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdateStatus(order.orderId, OrderStatus.CANCELLED) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text(
                                text = "Hủy đơn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Button(
                            onClick = { onUpdateStatus(order.orderId, nextStatus) },
                            modifier = Modifier.weight(1.8f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = actionColor ?: SellerGreen
                            )
                        ) {
                            if (actionIcon != null) {
                                Icon(
                                    imageVector = actionIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                               )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = actionLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onUpdateStatus(order.orderId, nextStatus) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionColor ?: SellerGreen
                        )
                    ) {
                        if (actionIcon != null) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = actionLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerOrderPreview() {
    val mockOrders = listOf(
        Order(
            orderId = "ord_abc123456",
            buyerName = "Nguyễn Văn A",
            status = OrderStatus.PENDING,
            totalAmount = 250000.0,
            items = listOf(
                OrderItem(productName = "Táo Mỹ", quantity = 2, unit = "kg", priceAtOrder = 75000.0),
                OrderItem(productName = "Chuối", quantity = 1, unit = "nải", priceAtOrder = 30000.0)
            )
        ),
        Order(
            orderId = "ord_def789012",
            buyerName = "Trần Thị B",
            status = OrderStatus.CONFIRMED,
            totalAmount = 150000.0,
            items = listOf(
                OrderItem(productName = "Rau muống", quantity = 3, unit = "bó", priceAtOrder = 15000.0)
            )
        ),
        Order(
            orderId = "ord_ghi345678",
            buyerName = "Lê Văn C",
            status = OrderStatus.DELIVERED,
            totalAmount = 320000.0,
            items = listOf(
                OrderItem(productName = "Thịt heo", quantity = 1, unit = "kg", priceAtOrder = 120000.0)
            )
        )
    )

    SellerOrderContent(
        orders = mockOrders,
        isLoading = false
    )
}
