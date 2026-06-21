package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.repository.WalletRepository
import com.google.firebase.auth.FirebaseAuth

private val WalletGreen = Color(0xFF22C55E)
private val WalletGreenLight = Color(0xFFDCFCE7)
private val BankBlue = Color(0xFF3B82F6)
private val BankBlueLight = Color(0xFFEFF6FF)

enum class PaymentMethod { TRANSFER, WALLET }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerCheckoutScreen(
    navController: NavController,
    viewModel: BuyerCartViewModel = viewModel()
) {
    val cartItems by viewModel.cartItems
    val selectedItemIds by viewModel.selectedItemIds
    val isLoading by viewModel.isLoading

    val selectedItems = cartItems.filter { selectedItemIds.contains(it.productId) }

    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.TRANSFER) }
    var walletBalance by remember { mutableStateOf(0.0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val totalPrice = selectedItems.sumOf { it.price * it.quantity }
    val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(totalPrice)
    val formattedBalance = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(walletBalance)
    val isWalletEnough = walletBalance >= totalPrice

    // Load số dư ví
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        WalletRepository().getBalance(uid).onSuccess { walletBalance = it }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đặt hàng", fontWeight = FontWeight.Bold, color = DarkNavy) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = DarkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thông tin giao hàng
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Thông tin giao hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Địa chỉ giao hàng (Bắt buộc)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                label = { Text("Ghi chú (Tùy chọn)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                            )
                        }
                    }
                }

                // Chọn phương thức thanh toán
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                            Spacer(Modifier.height(12.dp))

                            // Option: Chuyển khoản QR
                            PaymentMethodCard(
                                selected = paymentMethod == PaymentMethod.TRANSFER,
                                icon = Icons.Default.QrCode,
                                iconColor = BankBlue,
                                bgColor = if (paymentMethod == PaymentMethod.TRANSFER) BankBlueLight else Color(0xFFF5F5F5),
                                title = "Chuyển khoản ngân hàng",
                                subtitle = "Quét mã QR VietQR để thanh toán",
                                badge = null,
                                isDisabled = false,
                                onClick = { paymentMethod = PaymentMethod.TRANSFER }
                            )

                            Spacer(Modifier.height(10.dp))

                            // Option: Ví tiền
                            PaymentMethodCard(
                                selected = paymentMethod == PaymentMethod.WALLET,
                                icon = Icons.Default.AccountBalanceWallet,
                                iconColor = if (isWalletEnough) WalletGreen else Color(0xFF9E9E9E),
                                bgColor = when {
                                    paymentMethod == PaymentMethod.WALLET && isWalletEnough -> WalletGreenLight
                                    !isWalletEnough -> Color(0xFFF5F5F5)
                                    else -> Color(0xFFF5F5F5)
                                },
                                title = "Ví tiền",
                                subtitle = if (isWalletEnough)
                                    "Số dư: ${formattedBalance}đ"
                                else
                                    "Số dư không đủ (${formattedBalance}đ)",
                                badge = if (!isWalletEnough) "Không đủ" else null,
                                isDisabled = !isWalletEnough,
                                onClick = { if (isWalletEnough) paymentMethod = PaymentMethod.WALLET }
                            )
                        }
                    }
                }

                // Danh sách sản phẩm
                item {
                    Text("Danh sách sản phẩm (${selectedItems.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy, modifier = Modifier.padding(vertical = 8.dp))
                }

                items(selectedItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.Bold, color = DarkNavy, maxLines = 1)
                                val priceFmt = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(item.price)
                                Text("${priceFmt}đ x ${item.quantity}", color = Color.Gray, fontSize = 14.sp)
                            }
                            val totalItemFmt = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(item.price * item.quantity)
                            Text("${totalItemFmt}đ", color = AccentOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Thanh dưới – tổng tiền và nút đặt hàng
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng thanh toán", color = Color.Gray, fontSize = 13.sp)
                        Text("${formattedTotal}đ", fontWeight = FontWeight.Bold, color = AccentOrange, fontSize = 20.sp)
                        AnimatedVisibility(paymentMethod == PaymentMethod.WALLET) {
                            Text("Thanh toán bằng Ví", fontSize = 11.sp, color = WalletGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = {
                            if (address.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Vui lòng nhập địa chỉ giao hàng!") }
                                return@Button
                            }
                            when (paymentMethod) {
                                PaymentMethod.TRANSFER -> {
                                    viewModel.placeOrder(
                                        shippingAddress = address,
                                        note = note,
                                        onSuccess = { orderId, totalAmount, sellerId ->
                                            navController.navigate("payment_qr_screen/$orderId/$totalAmount/$sellerId") {
                                                popUpTo(BuyerRoutes.HOME) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        },
                                        onError = { err ->
                                            scope.launch { snackbarHostState.showSnackbar(err) }
                                        }
                                    )
                                }
                                PaymentMethod.WALLET -> {
                                    viewModel.placeOrderWithWallet(
                                        shippingAddress = address,
                                        note = note,
                                        onSuccess = { orderId, totalAmount ->
                                            navController.navigate("${BuyerRoutes.WALLET_SUCCESS}/$orderId/$totalAmount") {
                                                popUpTo(BuyerRoutes.HOME) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        },
                                        onError = { err ->
                                            scope.launch { snackbarHostState.showSnackbar(err) }
                                        }
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentMethod == PaymentMethod.WALLET) WalletGreen else AccentOrange
                        ),
                        modifier = Modifier.height(50.dp).width(140.dp),
                        enabled = !isLoading && selectedItems.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Đặt Hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    selected: Boolean,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    title: String,
    subtitle: String,
    badge: String?,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) iconColor else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isDisabled) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isDisabled) Color.Gray else DarkNavy
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDisabled) Color(0xFFBDBDBD) else Color.Gray
            )
        }
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Text(
                    badge,
                    fontSize = 10.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        if (selected) {
            Surface(shape = CircleShape, color = iconColor, modifier = Modifier.size(20.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
