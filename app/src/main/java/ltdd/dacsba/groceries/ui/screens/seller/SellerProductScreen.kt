package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.ui.components.SmartImage

@Composable
fun SellerProductScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val products by viewModel.products
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    SellerProductContent(
        products = products,
        isLoading = isLoading,
        onAddClick = {
            navController.navigate(ltdd.dacsba.groceries.data.constant.AppConstant.Routes.SELLER_ADD_PRODUCT)
        },
        onEditClick = { product ->
            navController.navigate("${ltdd.dacsba.groceries.data.constant.AppConstant.Routes.SELLER_EDIT_PRODUCT}/${product.id}")
        },
        onDeleteClick = { productId ->
            viewModel.deleteProduct(productId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductContent(
    products: List<Product>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var filterCategory by remember { mutableStateOf<ltdd.dacsba.groceries.data.model.Category?>(null) }
    val tabs = listOf("Đã duyệt", "Chờ duyệt", "Bị từ chối")
    
    val filteredProducts = products.filter {
        val matchesTab = when (selectedTab) {
            0 -> it.status == "APPROVED"
            1 -> it.status == "PENDING"
            2 -> it.status == "REJECTED"
            else -> false
        }
        val matchesCategory = filterCategory == null || it.categoryId == filterCategory?.categoryId
        matchesTab && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quản lý Sản phẩm",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults
                    .buttonColors(
                        containerColor = SellerGreen
                    ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp))

                Spacer(Modifier.width(4.dp))

                Text("Add", fontSize = 14.sp)
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = SellerGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SellerGreen
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
                            color = if (selectedTab == index) SellerGreen else Color.Gray
                        )
                    }
                )
            }
        }

        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filterCategory == null,
                    onClick = { filterCategory = null },
                    label = { Text("Tất cả") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SellerGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SellerGreen
                    )
                )
            }
            items(ltdd.dacsba.groceries.data.model.Category.defaultCategories) { cat ->
                val isSelected = filterCategory?.categoryId == cat.categoryId
                FilterChip(
                    selected = isSelected,
                    onClick = { filterCategory = cat },
                    label = { Text("${cat.iconEmoji} ${cat.categoryName}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SellerGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SellerGreen
                    )
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = SellerGreen
            )
        }

        if (filteredProducts.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có sản phẩm nào", color = Color.Gray)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts) { product ->
                ProductItemRow(
                    product = product,
                    onEdit = { onEditClick(product) },
                    onDelete = { onDeleteClick(product.id) }
                )
            }
        }
    }
}

@Composable
fun ProductItemRow(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    SmartImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(product.price)
                Text(
                    text = "${formattedPrice}đ / ${product.unit}",
                    color = SellerGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE8F5E9)) {
                        Text("Kho: ${product.stock}", fontSize = 10.sp, color = SellerGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                    val statusColor = when(product.status) {
                        "APPROVED" -> Color(0xFF1565C0)
                        "PENDING" -> Color(0xFFE65100)
                        else -> Color(0xFFD32F2F)
                    }
                    val statusBg = when(product.status) {
                        "APPROVED" -> Color(0xFFE3F2FD)
                        "PENDING" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFFFEBEE)
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = statusBg) {
                        Text(product.status, fontSize = 10.sp, color = statusColor, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerProductPreview() {
    val mockList = listOf(
        Product(id="1", name="Apple", price=50000.0, unit="kg", stock=100, soldCount = 10),
        Product(id="2", name="Banana", price=20000.0, unit="kg", stock=50, soldCount = 5)
    )
    SellerProductContent(
        products = mockList,
        isLoading = false,
        onAddClick = {},
        onEditClick = {},
        onDeleteClick = {}
    )
}