package ltdd.dacsba.groceries.ui.screens.user

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ltdd.dacsba.groceries.ui.components.SmartImage
import ltdd.dacsba.groceries.data.model.ProductTags
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import ltdd.dacsba.groceries.R
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.ui.screens.chat.ChatViewModel

val DarkNavy = Color(0xFF1B2430)
val AccentOrange = Color(0xFF787FF6)

@Composable
fun BuyerHomeScreen(
    viewModel: BuyerHomeViewModel = viewModel(), 
    navController: NavController? = null,
    parentNavController: NavController? = null
) {
    val requestResult by viewModel.requestResult
    val profileMessage by viewModel.profileMessage
    val snackbarHostState = remember { SnackbarHostState() }

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ltdd.dacsba.groceries.data.model.Product?>(null) }
    val sellerNames by viewModel.sellerNames
    val scope = rememberCoroutineScope()
    val chatViewModel: ChatViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    LaunchedEffect(requestResult) {
        requestResult?.let { snackbarHostState.showSnackbar(it); viewModel.clearRequestResult() }
    }
    LaunchedEffect(profileMessage) {
        profileMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearProfileMessage() }
    }

    if (selectedProduct != null) {
        ProductDetailSheet(
            product = selectedProduct!!,
            sellerName = sellerNames[selectedProduct!!.sellerId] ?: "",
            onDismiss = { selectedProduct = null },
            onAddToCart = { quantity ->
                val product = selectedProduct
                selectedProduct = null
                if (product != null) {
                    viewModel.addToCart(
                        product = product,
                        quantity = quantity,
                        onSuccess = {
                            scope.launch { snackbarHostState.showSnackbar("Đã thêm $quantity ${product.name} vào giỏ hàng") }
                        },
                        onError = { err ->
                            scope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    )
                }
            },
            onBuyNow = { quantity ->
                val product = selectedProduct
                selectedProduct = null
                if (product != null) {
                    viewModel.addToCart(
                        product = product,
                        quantity = quantity,
                        onSuccess = {
                            navController?.navigate(BuyerRoutes.CART)
                        },
                        onError = { err ->
                            scope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    )
                }
            },
            onChatWithSeller = { sellerId ->
                chatViewModel.getOrCreateChatRoom(sellerId) { roomId ->
                    if (roomId != null) {
                        selectedProduct = null
                        parentNavController?.navigate("chat_detail/$roomId/$sellerId") ?: navController?.navigate("chat_detail/$roomId/$sellerId")
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Không thể tạo phòng chat") }
                    }
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            BuyerHomeHeader(
                viewModel = viewModel,
                onSearchClick = { isSearchVisible = !isSearchVisible }
            )
            BuyerHomeBody(
                viewModel = viewModel,
                isSearchVisible = isSearchVisible,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onProductClick = { selectedProduct = it }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerHomeHeader(
    viewModel: BuyerHomeViewModel = viewModel(),
    onSearchClick: () -> Unit
) {
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
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Taut Shop",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                )
            }

            // Nút bên phải: Search
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {

                // ── Search ──
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFFF7F7F7),
                    shadowElevation = 2.dp
                ) {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                    }
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BuyerHomeBody(
    viewModel: BuyerHomeViewModel = viewModel(),
    isSearchVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (ltdd.dacsba.groceries.data.model.Product) -> Unit
) {
    val products by viewModel.products
    val isLoading by viewModel.isLoading
    val sellerNames by viewModel.sellerNames
    val recommendedProducts by viewModel.recommendedProducts
    val userTagProfile by viewModel.userTagProfile
    val isLoadingRecommendations by viewModel.isLoadingRecommendations

    var selectedCategory by remember { mutableStateOf<ltdd.dacsba.groceries.data.model.Category?>(null) }
    var sortOrder by remember { mutableStateOf(0) } // 0: None, 1: Low to High, 2: High to Low

    val approvedProducts = products.filter { it.status == "APPROVED" }

    var filtered = approvedProducts.filter { p ->
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || p.categoryId == selectedCategory?.categoryId
        matchesSearch && matchesCategory
    }
    filtered = when (sortOrder) {
        1 -> filtered.sortedBy { it.price }
        2 -> filtered.sortedByDescending { it.price }
        else -> filtered
    }

    val tabs = listOf("🛍️ Sản phẩm", "✨ Dành cho bạn")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {

        // ── Search Bar ───────────────────────────────────────────────────────────
        androidx.compose.animation.AnimatedVisibility(visible = isSearchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 8.dp),
                placeholder = { Text("Tìm kiếm sản phẩm...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentOrange,
                    unfocusedContainerColor = Color(0xFFF7F7F7),
                    focusedContainerColor = Color(0xFFF7F7F7)
                )
            )
        }

        // ── TabRow ─────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.White,
            contentColor = AccentOrange
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (pagerState.currentPage == index) AccentOrange else Color.Gray
                        )
                    }
                )
            }
        }

        // ── Pages ──────────────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                // ══ Tab 0: Sản phẩm ══════════════════════════════════════════════════
                0 -> Column(modifier = Modifier.fillMaxSize()) {
                    // Filter + Sort chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = sortOrder == 1,
                                onClick = { sortOrder = if (sortOrder == 1) 0 else 1 },
                                label = { Text("Giá thấp ↑", fontSize = 12.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = sortOrder == 2,
                                onClick = { sortOrder = if (sortOrder == 2) 0 else 2 },
                                label = { Text("Giá cao ↓", fontSize = 12.sp) }
                            )
                        }
                        item {
                            Spacer(Modifier.width(4.dp))
                            Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.LightGray))
                            Spacer(Modifier.width(4.dp))
                        }
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Tất cả", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentOrange
                                )
                            )
                        }
                        items(ltdd.dacsba.groceries.data.model.Category.defaultCategories) { cat ->
                            FilterChip(
                                selected = selectedCategory?.categoryId == cat.categoryId,
                                onClick = { selectedCategory = cat },
                                label = { Text("${cat.iconEmoji} ${cat.categoryName}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentOrange
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sản phẩm (${filtered.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 4.dp, top = 4.dp)
                    )

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentOrange)
                    } else if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy sản phẩm nào", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered) { product ->
                                ProductCard(
                                    product = product,
                                    sellerName = sellerNames[product.sellerId] ?: "Shop",
                                    onClick = { onProductClick(product) }
                                )
                            }
                        }
                    }
                }

                // ══ Tab 1: Dành cho bạn ═══════════════════════════════════════════
                1 -> Column(modifier = Modifier.fillMaxSize()) {
                    // Tag pills lý do gợi ý
                    if (userTagProfile.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Gợi ý dựa trên sở thích của bạn:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            userTagProfile.take(6).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = AccentOrange.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = ProductTags.displayNameOf(tag),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentOrange,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }

                    when {
                        isLoadingRecommendations -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(color = AccentOrange)
                                    Text(
                                        "Đang tìm sản phẩm phù hợp...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        recommendedProducts.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🛒", fontSize = 40.sp)
                                    Text(
                                        "Chưa có gợi ý nào",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1B2430)
                                    )
                                    Text(
                                        "Mua sắm thêm để chúng tôi hiểu sở thích của bạn 😊",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 32.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "${recommendedProducts.size} sản phẩm dành riêng cho bạn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(recommendedProducts, key = { it.id }) { product ->
                                    ProductCard(
                                        product = product,
                                        sellerName = sellerNames[product.sellerId] ?: "Shop",
                                        onClick = { onProductClick(product) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, sellerName: String = "", onClick: () -> Unit) {
    val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(product.price)
    val isOutOfStock = product.stock <= 0

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Hình ảnh + badge hết hàng
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    SmartImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.LightGray)
                }
                // Overlay hết hàng
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFFEF4444),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Hết hàng",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                color = if (isOutOfStock) Color.Gray else DarkNavy,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            // Tên shop & Đã bán
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sellerName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = sellerName,
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "Đã bán ${product.soldCount}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formattedPrice}đ/${product.unit}",
                    fontWeight = FontWeight.Bold,
                    color = if (isOutOfStock) Color.Gray else AccentOrange,
                    fontSize = 13.sp
                )
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(enabled = !isOutOfStock) { onClick() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOutOfStock) Color(0xFFE0E0E0) else AccentOrange
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isOutOfStock) Color.Gray else Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: ltdd.dacsba.groceries.data.model.Product,
    sellerName: String = "",
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onBuyNow: (Int) -> Unit,
    onChatWithSeller: (String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(product.price)
    val totalPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(product.price * quantity)
    val isOutOfStock = product.stock <= 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE0E0E0))
                    .align(Alignment.CenterHorizontally)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageUrl.isNotBlank()) {
                        SmartImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.LightGray)
                    }
                }

                Column {
                    Text(text = product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    // Tên shop
                    if (sellerName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = sellerName, fontSize = 13.sp, color = Color(0xFF9E9E9E))
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { onChatWithSeller(product.sellerId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_messenger),
                                    contentDescription = "Nhắn tin",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = "${formattedPrice}đ / ${product.unit}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOutOfStock) Color.Gray else AccentOrange
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isOutOfStock) {
                        Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "⚠️ Hết hàng",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Kho: ${product.stock}", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(width = 1.dp, height = 12.dp).background(Color.LightGray))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Đã bán: ${product.soldCount}", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            if (product.description.isNotBlank()) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Mô tả sản phẩm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            if (!isOutOfStock) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Số lượng", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp).clickable(enabled = quantity > 1) { quantity-- },
                            shape = CircleShape,
                            color = if (quantity > 1) Color(0xFFF0F0F0) else Color(0xFFFAFAFA)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                    color = if (quantity > 1) Color.Black else Color.LightGray)
                            }
                        }
                        Text(text = quantity.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            modifier = Modifier.size(32.dp).clickable(enabled = quantity < product.stock) { quantity++ },
                            shape = CircleShape,
                            color = if (quantity < product.stock) Color(0xFFF0F0F0) else Color(0xFFFAFAFA)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                    color = if (quantity < product.stock) Color.Black else Color.LightGray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAddToCart(quantity) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Thêm vào giỏ", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onBuyNow(quantity) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Text("Mua ngay - ${totalPrice}đ", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Hết hàng — chỉ hiển thông báo
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Sản phẩm này đã hết hàng, vui lòng quay lại sau.",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
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