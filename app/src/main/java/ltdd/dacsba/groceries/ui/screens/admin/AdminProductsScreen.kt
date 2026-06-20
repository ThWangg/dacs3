package ltdd.dacsba.groceries.ui.screens.admin

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.ui.components.ImagePickerButton
import ltdd.dacsba.groceries.ui.components.SmartImage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminProductsScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val products by viewModel.products
    val isLoading by viewModel.isLoading
    val isUploading by viewModel.isUploading

    AdminProductsContent(
        products = products,
        isLoading = isLoading,
        isUploading = isUploading,
        onRefresh = { viewModel.loadAll() },
        onAdd = { product -> viewModel.addProduct(product) },
        onEdit = { product -> viewModel.updateProduct(product) },
        onDelete = { productId -> viewModel.deleteProduct(productId) },
        onSeedData = { viewModel.seedSampleDataIfEmpty() },
        onUploadImage = { uri, onDone -> viewModel.uploadImage(uri, "products", onDone) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsContent(
    products: List<Product>,
    isLoading: Boolean,
    isUploading: Boolean = false,
    onRefresh: () -> Unit,
    onAdd: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (String) -> Unit,
    onSeedData: () -> Unit,
    onUploadImage: (Uri, (String) -> Unit) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var deletingProduct by remember { mutableStateOf<Product?>(null) }
    var filterCategory by remember { mutableStateOf<Category?>(null) }

    val filtered = products.filter { p -> 
        val matchesQuery = p.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = filterCategory == null || p.categoryId == filterCategory?.categoryId
        matchesQuery && matchesCategory
    }

deletingProduct?.let { p ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            title = { Text("Xóa sản phẩm?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa \"${p.name}\" không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(p.id); deletingProduct = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Xóa", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) { Text("Huỷ") }
            }
        )
    }

if (showAddSheet) {
        ProductFormSheet(
            product = null,
            isUploading = isUploading,
            onUploadImage = onUploadImage,
            onSave = { p -> onAdd(p); showAddSheet = false },
            onDismiss = { showAddSheet = false }
        )
    }

editingProduct?.let { p ->
        ProductFormSheet(
            product = p,
            isUploading = isUploading,
            onUploadImage = onUploadImage,
            onSave = { updated -> onEdit(updated); editingProduct = null },
            onDismiss = { editingProduct = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(AdminBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(AdminGreen, AdminGreenLight)))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text("Quản lý Sản phẩm", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("${products.size} sản phẩm", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    }
                }
            }

OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Tìm tên sản phẩm...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdminGreenLight,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterCategory == null,
                        onClick = { filterCategory = null },
                        label = { Text("Tất cả") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdminGreenLight.copy(alpha = 0.2f),
                            selectedLabelColor = AdminGreen
                        )
                    )
                }
                items(Category.defaultCategories) { cat ->
                    val isSelected = filterCategory?.categoryId == cat.categoryId
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterCategory = cat },
                        label = { Text("${cat.iconEmoji} ${cat.categoryName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdminGreenLight.copy(alpha = 0.2f),
                            selectedLabelColor = AdminGreen
                        )
                    )
                }
            }

            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AdminGreenLight)

            if (filtered.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có sản phẩm", color = Color.Gray, fontSize = 15.sp)

                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { product ->
                        AdminProductCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { deletingProduct = product }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

}
}

@Composable
fun AdminProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(product.price)
    val categoryName = Category.defaultCategories.find { it.categoryId == product.categoryId }?.let {
        "${it.iconEmoji} ${it.categoryName}"
    } ?: product.categoryId

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0F4F0)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    SmartImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(categoryName, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                Text("${formattedPrice}đ/${product.unit}", color = AdminGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoChip("Kho: ${product.stock}", Color(0xFFE8F5E9), AdminGreen)
                    InfoChip("Bán: ${product.soldCount}", Color(0xFFFFF3E0), Color(0xFFE65100))
                    if (product.ratingAverage > 0) InfoChip("⭐ ${product.ratingAverage}", Color(0xFFFFF9C4), Color(0xFFF57F17))
                }
            }

}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormSheet(
    product: Product?,
    isUploading: Boolean = false,
    onUploadImage: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    onSave: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = product != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(if (product != null) product.price.toString() else "") }
    var stock by remember { mutableStateOf(if (product != null) product.stock.toString() else "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var selectedCategory by remember { mutableStateOf(Category.defaultCategories.find { it.categoryId == product?.categoryId } ?: Category.defaultCategories.first()) }
    var selectedUnit by remember { mutableStateOf(product?.unit ?: selectedCategory.availableUnits.first()) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0)).align(Alignment.CenterHorizontally))

            Text(
                if (isEdit) "✏️ Chỉnh sửa sản phẩm" else "➕ Thêm sản phẩm mới",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Tên sản phẩm *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = nameError,
                supportingText = if (nameError) ({ Text("Vui lòng nhập tên sản phẩm") }) else null,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminGreen)
            )

Text("Danh mục *", fontSize = 13.sp, color = Color.Gray)
            ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) {
                OutlinedTextField(
                    value = "${selectedCategory.iconEmoji} ${selectedCategory.categoryName}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminGreen)
                )
                ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                    Category.defaultCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.iconEmoji} ${cat.categoryName}") },
                            onClick = {
                                selectedCategory = cat
                                selectedUnit = cat.availableUnits.first()
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; priceError = false },
                    label = { Text("Giá (đ) *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isError = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminGreen)
                )

                ExposedDropdownMenuBox(expanded = showUnitMenu, onExpandedChange = { showUnitMenu = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Đơn vị") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminGreen)
                    )
                    ExposedDropdownMenu(expanded = showUnitMenu, onDismissRequest = { showUnitMenu = false }) {
                        selectedCategory.availableUnits.forEach { unit ->
                            DropdownMenuItem(text = { Text(unit) }, onClick = { selectedUnit = unit; showUnitMenu = false })
                        }
                    }
                }
            }

OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Tồn kho") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminGreen)
            )

Text("Ảnh sản phẩm", fontSize = 13.sp, color = Color.Gray)
            ImagePickerButton(
                currentImageUrl = imageUrl,
                isUploading = isUploading,
                onImagePicked = { uri ->

                    imageUrl = uri.toString()
                    onUploadImage(uri) { downloadUrl -> imageUrl = downloadUrl }
                },
                onRemoveImage = { imageUrl = "" },
                label = "Bấm để chọn ảnh từ thư viện",
                previewHeight = 160.dp,
                accentColor = AdminGreen
            )

OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AdminGreen)
            )

            Spacer(Modifier.height(4.dp))

Button(
                onClick = {
                    nameError = name.isBlank()
                    priceError = price.toDoubleOrNull() == null
                    if (nameError || priceError) return@Button

                    val saved = (product ?: Product()).copy(
                        name = name.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = selectedUnit,
                        stock = stock.toIntOrNull() ?: 0,
                        description = description.trim(),
                        imageUrl = imageUrl.trim(),
                        categoryId = selectedCategory.categoryId,
                        createdAt = product?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(saved)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "Lưu thay đổi" else "Thêm sản phẩm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Huỷ", color = Color.Gray)
            }
        }
    }
}

@Composable
fun InfoChip(text: String, bg: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(text, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminProductsPreview() {
    AdminProductsContent(
        products = listOf(
            Product(id = "1", name = "Táo Fuji Nhật", price = 89000.0, unit = "kg", categoryId = "fresh_fruit", stock = 150, soldCount = 320, ratingAverage = 4.8),
            Product(id = "2", name = "Tôm Sú Tươi", price = 220000.0, unit = "kg", categoryId = "meat_seafood", stock = 40, soldCount = 95, ratingAverage = 4.7),
        ),
        isLoading = false,
        onRefresh = {}, onAdd = {}, onEdit = {}, onDelete = {}, onSeedData = {}
    )
}
