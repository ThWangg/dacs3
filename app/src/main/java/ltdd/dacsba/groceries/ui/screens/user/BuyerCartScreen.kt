package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.data.model.CartItem
import ltdd.dacsba.groceries.ui.components.SmartImage
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerCartScreen(
    navController: NavController,
    viewModel: BuyerCartViewModel = viewModel()
) {
    val cartItems by viewModel.cartItems
    val selectedItemIds by viewModel.selectedItemIds
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng", fontWeight = FontWeight.Bold, color = DarkNavy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("Giỏ hàng đang trống", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                }
            }
        } else {
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
                    items(cartItems) { item ->
                        val isSelected = selectedItemIds.contains(item.productId)
                        CartItemRow(
                            item = item,
                            isSelected = isSelected,
                            onToggleSelect = { viewModel.toggleSelection(item.productId) },
                            onRemove = { viewModel.removeFromCart(item.productId) },
                            onQuantityChange = { newQty ->
                                viewModel.updateQuantity(item.productId, newQty) { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
                
                val selectedItems = cartItems.filter { selectedItemIds.contains(it.productId) }
                val isAllSelected = cartItems.isNotEmpty() && selectedItemIds.size == cartItems.size

                CartBottomBar(
                    selectedItems = selectedItems,
                    isAllSelected = isAllSelected,
                    onToggleAll = { viewModel.selectAll(!isAllSelected) },
                    onCheckout = {
                        navController.navigate(BuyerRoutes.CHECKOUT)
                    }
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem, 
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(item.price)
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
            )
            
            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (item.productImageUrl.isNotBlank()) {
                    SmartImage(
                        model = item.productImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.productName, fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 16.sp, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${formattedPrice}đ", color = AccentOrange, fontWeight = FontWeight.SemiBold)

Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onQuantityChange(item.quantity - 1) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF2F2F7)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                            }
                        }
                        
                        Text(text = item.quantity.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onQuantityChange(item.quantity + 1) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF2F2F7)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun CartBottomBar(
    selectedItems: List<CartItem>, 
    isAllSelected: Boolean,
    onToggleAll: () -> Unit,
    onCheckout: () -> Unit
) {
    val totalPrice = selectedItems.sumOf { it.price * it.quantity }
    val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(totalPrice)
    
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isAllSelected,
                    onCheckedChange = { onToggleAll() },
                    colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
                )
                Spacer(Modifier.width(4.dp))
                Text("Tất cả", fontSize = 14.sp)
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Tổng thanh toán", color = Color.Gray, fontSize = 13.sp)
                Text(
                    text = "${formattedTotal}đ",
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    fontSize = 20.sp
                )
            }
            
            Button(
                onClick = onCheckout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                modifier = Modifier.height(50.dp),
                enabled = selectedItems.isNotEmpty()
            ) {
                Text("Mua Hàng (${selectedItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
