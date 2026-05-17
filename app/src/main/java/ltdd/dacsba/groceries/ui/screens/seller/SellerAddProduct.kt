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
import com.google.firebase.auth.FirebaseAuth
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.ui.components.AppDropdown
import ltdd.dacsba.groceries.ui.components.AppTextField
import java.util.UUID

@Composable
fun SellerAddProductScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading
    val addSuccess by viewModel.addSuccess
    val message by viewModel.message

    LaunchedEffect(addSuccess) {
        if (addSuccess) {
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
            viewModel.addSuccess.value = false
            navController.popBackStack()
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.message.value = null
        }
    }

    AddProductContent(
        isLoading = isLoading,
        onBack = { navController.popBackStack() },
        onAddClick = { name, desc, price, unit, stock, catId, imgUrl ->
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val newProduct = Product(
                id = UUID.randomUUID().toString(),
                name = name,
                description = desc,
                price = price.toDoubleOrNull() ?: 0.0,
                unit = unit,
                stock = stock.toIntOrNull() ?: 0,
                categoryId = catId,
                imageUrl = imgUrl,
                sellerId = currentUserId
            )
            viewModel.addProduct(newProduct)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductContent(
    isLoading: Boolean,
    onBack: () -> Unit,
    onAddClick: (String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUnit by remember { mutableStateOf("") }

    val categoryNames = Category.defaultCategories.map { it.categoryName }
    val unitOptions = selectedCategory?.availableUnits ?: Category.allUnits

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add new product",
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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
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
                    //reset unit nếu unit k tồn tại
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
                label = "Ímage",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onAddClick(
                        name,
                        description,
                        price,
                        selectedUnit,
                        stock,
                        selectedCategory?.categoryId ?: "",
                        imageUrl
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && name.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Product")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProdPreview() {
    AddProductContent(
        isLoading = false,
        onBack = {},
        onAddClick = { _, _, _, _, _, _, _ -> }
    )
}
