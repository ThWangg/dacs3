package ltdd.dacsba.groceries.ui.screens.seller

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.repository.ImageUtils
import ltdd.dacsba.groceries.ui.components.ImagePickerButton
import ltdd.dacsba.groceries.ui.components.TagSelectorSection
import ltdd.dacsba.groceries.data.model.ProductTags
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerAddProductScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading
    val addSuccess by viewModel.addSuccess
    val message by viewModel.message
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(addSuccess) {
        if (addSuccess) {
            Toast.makeText(context, "Đã gửi yêu cầu thêm sản phẩm. Vui lòng chờ Admin duyệt!", Toast.LENGTH_LONG).show()
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

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.defaultCategories.first()) }
    var selectedUnit by remember { mutableStateOf(selectedCategory.availableUnits.first()) }
    
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }

    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var isImageConverting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm sản phẩm mới", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Tên sản phẩm *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = nameError,
                supportingText = if (nameError) ({ Text("Vui lòng nhập tên sản phẩm") }) else null,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SellerGreen)
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SellerGreen)
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

TagSelectorSection(
                categoryId = selectedCategory.categoryId,
                selectedTags = selectedTags,
                onTagsChanged = { selectedTags = it },
                accentColor = SellerGreen
            )

Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; priceError = false },
                    label = { Text("Giá (đ) *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isError = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SellerGreen)
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
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SellerGreen)
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
                label = { Text("Tồn kho ban đầu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SellerGreen)
            )

Text("Ảnh sản phẩm (Tùy chọn)", fontSize = 13.sp, color = Color.Gray)
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
                accentColor = SellerGreen
            )

OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SellerGreen)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    nameError = name.isBlank()
                    priceError = price.toDoubleOrNull() == null
                    if (nameError || priceError) return@Button

                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val autoTags = ProductTags.generateAutoTags(selectedCategory.categoryId, name.trim())
                    val finalTags = (selectedTags + autoTags).distinct().take(ProductTags.MAX_TAGS)

                    val newProduct = Product(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        description = description.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        unit = selectedUnit,
                        stock = stock.toIntOrNull() ?: 0,
                        categoryId = selectedCategory.categoryId,
                        imageUrl = imageUrl,
                        sellerId = currentUserId,
                        status = "PENDING",
                        tags = finalTags
                    )
                    viewModel.addProduct(newProduct)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading && !isImageConverting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SellerGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gửi yêu cầu Thêm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
