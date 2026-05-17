package ltdd.dacsba.groceries.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ltdd.dacsba.groceries.data.model.Category

private val AdminPrimary = Color(0xFF787FF6)
private val AdminSecondary = Color(0xFF1CA7EC)
private val AdminDark = Color(0xFF1F2F98)

// ===================== Screen (kết nối ViewModel) =====================

@Composable
fun AdminCategoryScreen(
    viewModel: AdminCategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val categoryList by viewModel.categoryList
    val isLoading by viewModel.isLoading
    val isDialogVisible by viewModel.isDialogVisible
    val editingCategory by viewModel.editingCategory
    val dialogCategoryName by viewModel.dialogCategoryName
    val dialogIconEmoji by viewModel.dialogIconEmoji
    val dialogAvailableUnitsText by viewModel.dialogAvailableUnitsText
    val actionMessage by viewModel.actionMessage

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    // Dialog thêm/sửa danh mục
    if (isDialogVisible) {
        CategoryEditDialog(
            isEditMode = editingCategory != null,
            categoryName = dialogCategoryName,
            iconEmoji = dialogIconEmoji,
            availableUnitsText = dialogAvailableUnitsText,
            onCategoryNameChange = { viewModel.onCategoryNameChange(it) },
            onIconEmojiChange = { viewModel.onIconEmojiChange(it) },
            onAvailableUnitsTextChange = { viewModel.onAvailableUnitsTextChange(it) },
            onConfirm = { viewModel.saveCategory() },
            onDismiss = { viewModel.closeDialog() }
        )
    }

    AdminCategoryContent(
        categoryList = categoryList,
        isLoading = isLoading,
        onAddClick = { viewModel.openAddDialog() },
        onEditClick = { viewModel.openEditDialog(it) },
        onDeleteClick = { viewModel.deleteCategory(it) }
    )
}

// ===================== UI =====================

@Composable
fun AdminCategoryContent(
    categoryList: List<Category>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = AdminPrimary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm danh mục")
            }
        },
        containerColor = Color(0xFFF3F4FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(AdminPrimary, AdminDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "Quản lý danh mục",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${categoryList.size} danh mục",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AdminPrimary
                )
            }

            if (categoryList.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chưa có danh mục nào", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryList) { category ->
                        CategoryManagementCard(
                            category = category,
                            onEdit = { onEditClick(category) },
                            onDelete = { onDeleteClick(category.categoryId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryManagementCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa danh mục?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn chắc chắn muốn xóa danh mục \"${category.categoryName}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon emoji
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color(0xFFE8EAFD),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.iconEmoji.ifBlank { "📦" },
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.categoryName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${category.availableUnits.size} đơn vị: ${category.availableUnits.take(3).joinToString(", ")}${if (category.availableUnits.size > 3) "..." else ""}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Nút sửa
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa",
                    tint = AdminPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Nút xóa
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryEditDialog(
    isEditMode: Boolean,
    categoryName: String,
    iconEmoji: String,
    availableUnitsText: String,
    onCategoryNameChange: (String) -> Unit,
    onIconEmojiChange: (String) -> Unit,
    onAvailableUnitsTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Chỉnh sửa danh mục" else "Thêm danh mục mới",
                fontWeight = FontWeight.Bold,
                color = AdminDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = onCategoryNameChange,
                    label = { Text("Tên danh mục") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimary
                    )
                )
                OutlinedTextField(
                    value = iconEmoji,
                    onValueChange = onIconEmojiChange,
                    label = { Text("Icon Emoji (vd: 🍎)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimary
                    )
                )
                OutlinedTextField(
                    value = availableUnitsText,
                    onValueChange = onAvailableUnitsTextChange,
                    label = { Text("Đơn vị (cách nhau bằng dấu phẩy)") },
                    placeholder = { Text("kg, g, hộp, túi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
            ) {
                Text(if (isEditMode) "Lưu thay đổi" else "Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

// ===================== Preview =====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminCategoryPreview() {
    AdminCategoryContent(
        categoryList = Category.defaultCategories,
        isLoading = false,
        onAddClick = {},
        onEditClick = {},
        onDeleteClick = {}
    )
}
