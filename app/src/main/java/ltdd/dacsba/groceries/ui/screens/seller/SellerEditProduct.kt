package ltdd.dacsba.groceries.ui.screens.seller

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.ui.components.AppDropdown
import ltdd.dacsba.groceries.ui.components.AppTextField
import ltdd.dacsba.groceries.ui.components.ImagePickerButton
import ltdd.dacsba.groceries.ui.components.TagSelectorSection
import ltdd.dacsba.groceries.data.model.ProductTags
import ltdd.dacsba.groceries.data.repository.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color

@Composable
fun SellerEditProductScreen(
    navController: NavController,
    productId: String?,
    viewModel: SellerViewModel = viewModel()
) {
    val context = LocalContext.current
    val productToEdit = viewModel.products.value.find { it.id == productId }
    val isLoading by viewModel.isLoading
    val updateSuccess by viewModel.updateSuccess

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            viewModel.updateSuccess.value = false
            navController.popBackStack()
        }
    }

    productToEdit?.let { product ->
        EditProductContent(
            product = product,
            isLoading = isLoading,
            onBack = { navController.popBackStack() },
            onSaveClick = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductContent(
    product: Product,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSaveClick: (Product) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isImageConverting by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(product.name) }
    var description by remember { mutableStateOf(product.description) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var imageUrl by remember { mutableStateOf(product.imageUrl) }

    val initialCategory = Category.defaultCategories.find { it.categoryId == product.categoryId }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedUnit by remember { mutableStateOf(product.unit) }

    val categoryNames = Category.defaultCategories.map { it.categoryName }
    val unitOptions = selectedCategory?.availableUnits ?: Category.allUnits

    var selectedTags by remember { mutableStateOf(product.tags) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa sản phẩm",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Tên sản phẩm",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả",
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Đơn giá (đ)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                AppDropdown(
                    selectedValue = selectedUnit,
                    onValueSelected = { selectedUnit = it },
                    label = "Đơn vị",
                    options = unitOptions,
                    modifier = Modifier.weight(1f)
                )
            }

            AppTextField(
                value = stock,
                onValueChange = { stock = it },
                label = "Sản luợng",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AppDropdown(
                selectedValue = selectedCategory?.categoryName ?: "",
                onValueSelected = { pickedName ->
                    val category = Category.defaultCategories.find { it.categoryName == pickedName }
                    selectedCategory = category
                    if (category != null && selectedUnit !in category.availableUnits) {
                        selectedUnit = ""
                    }
                },
                label = "Loại",
                options = categoryNames,
                modifier = Modifier.fillMaxWidth()
            )

            TagSelectorSection(
                categoryId = selectedCategory?.categoryId ?: "",
                selectedTags = selectedTags,
                onTagsChanged = { selectedTags = it },
                accentColor = Color(0xFF7CB342) // SellerGreen
            )

            Text("Ảnh sản phẩm", fontSize = 13.sp, color = Color.Gray)
            ImagePickerButton(
                currentImageUrl = imageUrl,
                isUploading = isImageConverting,
                onImagePicked = { uri ->
                    coroutineScope.launch {
                        isImageConverting = true
                        val base64 = try {
                            withContext(Dispatchers.IO) {
                                ImageUtils.uriToBase64(context, uri)
                            }
                        } catch (e: Exception) { null }
                        if (base64 != null) {
                            imageUrl = base64
                        } else {
                            Toast.makeText(context, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show()
                        }
                        isImageConverting = false
                    }
                },
                onRemoveImage = { imageUrl = "" },
                label = "Bấm để chọn ảnh từ thư viện",
                previewHeight = 160.dp,
                accentColor = Color(0xFF7CB342) // SellerGreen
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val catId = selectedCategory?.categoryId ?: product.categoryId
                    val autoTags = ProductTags.generateAutoTags(catId, name)
                    val finalTags = (selectedTags + autoTags).distinct().take(ProductTags.MAX_TAGS)

                    val updated = product.copy(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = selectedUnit,
                        stock = stock.toIntOrNull() ?: 0,
                        categoryId = catId,
                        imageUrl = imageUrl,
                        tags = finalTags
                    )
                    onSaveClick(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isImageConverting
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Lưu thay đổi")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProdPreview() {
    val mockProduct = Product(
        id = "123",
        name = "Táo Mỹ Envy",
        description = "Táo nhập khẩu tươi ngon, giòn ngọt.",
        price = 150000.0,
        unit = "kg",
        stock = 50,
        categoryId = "fresh_fruit",
        imageUrl = "https://example.com/apple.jpg"
    )

    EditProductContent(
        product = mockProduct,
        isLoading = false,
        onBack = {},
        onSaveClick = {}
    )
}