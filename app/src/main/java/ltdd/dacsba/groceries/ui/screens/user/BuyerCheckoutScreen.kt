package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.launch

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
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val totalPrice = selectedItems.sumOf { it.price * it.quantity }
    val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(totalPrice)

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
                    }
                    Button(
                        onClick = {
                            if (address.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Vui lòng nhập địa chỉ giao hàng!") }
                                return@Button
                            }
                            viewModel.placeOrder(
                                shippingAddress = address,
                                note = note,
                                onSuccess = { orderId, totalAmount, sellerId ->
                                    navController.navigate(
                                        "payment_qr_screen/$orderId/$totalAmount/$sellerId"
                                    ) {
                                        popUpTo(BuyerRoutes.HOME) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onError = { err ->
                                    scope.launch { snackbarHostState.showSnackbar(err) }
                                }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
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
