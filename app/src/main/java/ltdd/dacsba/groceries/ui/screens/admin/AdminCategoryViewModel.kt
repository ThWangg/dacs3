package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.Category
import ltdd.dacsba.groceries.data.repository.AdminRepository

class AdminCategoryViewModel : ViewModel() {
    private val adminRepository = AdminRepository()

    var categoryList = mutableStateOf<List<Category>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var actionMessage = mutableStateOf<String?>(null)
        private set

    // Trạng thái dialog thêm/sửa
    var isDialogVisible = mutableStateOf(false)
        private set

    // null = thêm mới, không null = chỉnh sửa
    var editingCategory = mutableStateOf<Category?>(null)
        private set

    // Input trong dialog
    var dialogCategoryName = mutableStateOf("")
        private set

    var dialogIconEmoji = mutableStateOf("")
        private set

    // Đơn vị nhập dạng chuỗi phân cách bằng dấu phẩy
    var dialogAvailableUnitsText = mutableStateOf("")
        private set

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            isLoading.value = true
            val result = adminRepository.getAllCategories()
            result.onSuccess { categories ->
                categoryList.value = categories
            }.onFailure { error ->
                actionMessage.value = "Lỗi tải danh mục: ${error.message}"
            }
            isLoading.value = false
        }
    }

    // Mở dialog thêm danh mục mới
    fun openAddDialog() {
        editingCategory.value = null
        dialogCategoryName.value = ""
        dialogIconEmoji.value = ""
        dialogAvailableUnitsText.value = ""
        isDialogVisible.value = true
    }

    // Mở dialog chỉnh sửa danh mục đã có
    fun openEditDialog(category: Category) {
        editingCategory.value = category
        dialogCategoryName.value = category.categoryName
        dialogIconEmoji.value = category.iconEmoji
        dialogAvailableUnitsText.value = category.availableUnits.joinToString(", ")
        isDialogVisible.value = true
    }

    fun closeDialog() {
        isDialogVisible.value = false
        editingCategory.value = null
    }

    fun onCategoryNameChange(value: String) {
        dialogCategoryName.value = value
    }

    fun onIconEmojiChange(value: String) {
        dialogIconEmoji.value = value
    }

    fun onAvailableUnitsTextChange(value: String) {
        dialogAvailableUnitsText.value = value
    }

    fun saveCategory() {
        val name = dialogCategoryName.value.trim()
        val emoji = dialogIconEmoji.value.trim()

        if (name.isEmpty()) {
            actionMessage.value = "Tên danh mục không được để trống"
            return
        }

        val units = dialogAvailableUnitsText.value
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val existingCategory = editingCategory.value
        val categoryToSave = if (existingCategory != null) {
            // Chỉnh sửa: giữ nguyên categoryId
            existingCategory.copy(
                categoryName = name,
                iconEmoji = emoji,
                availableUnits = units
            )
        } else {
            // Thêm mới: tạo ID từ tên
            Category(
                categoryId = name.lowercase().replace(" ", "_")
                    + "_${System.currentTimeMillis()}",
                categoryName = name,
                iconEmoji = emoji,
                availableUnits = units
            )
        }

        viewModelScope.launch {
            isLoading.value = true
            val result = adminRepository.addOrUpdateCategory(categoryToSave)
            result.onSuccess {
                actionMessage.value = if (existingCategory != null) {
                    "Đã cập nhật danh mục"
                } else {
                    "Đã thêm danh mục mới"
                }
                closeDialog()
                loadCategories()
            }.onFailure { error ->
                actionMessage.value = "Lỗi lưu danh mục: ${error.message}"
            }
            isLoading.value = false
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val result = adminRepository.deleteCategory(categoryId)
            result.onSuccess {
                actionMessage.value = "Đã xóa danh mục"
                loadCategories()
            }.onFailure { error ->
                actionMessage.value = "Lỗi xóa: ${error.message}"
            }
        }
    }

    fun clearActionMessage() {
        actionMessage.value = null
    }
}
