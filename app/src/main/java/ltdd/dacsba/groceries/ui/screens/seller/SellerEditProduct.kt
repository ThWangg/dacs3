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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.ui.components.AppDropdown
import ltdd.dacsba.groceries.ui.components.AppTextField

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Product name",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Price (đ)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                AppDropdown(
                    selectedValue = selectedUnit,
                    onValueSelected = { selectedUnit = it },
                    label = "Unit",
                    options = unitOptions,
                    modifier = Modifier.weight(1f)
                )
            }

            AppTextField(
                value = stock,
                onValueChange = { stock = it },
                label = "Stock",
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
                label = "Category",
                options = categoryNames,
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = "Image",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val updated = product.copy(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = selectedUnit,
                        stock = stock.toIntOrNull() ?: 0,
                        categoryId = selectedCategory?.categoryId ?: product.categoryId,
                        imageUrl = imageUrl
                    )
                    onSaveClick(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes")
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