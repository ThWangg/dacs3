package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.Product
import ltdd.dacsba.groceries.data.repository.ProductRepository

class BuyerHomeViewModel(): ViewModel() {
    private val productRepository = ProductRepository()

    var products = mutableStateOf<List<Product>>(emptyList())
    var isLoading = mutableStateOf(false)
    var message = mutableStateOf<String?>(null)

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            isLoading.value = true
            message.value = null
            val result = productRepository.getAllProducts()

            result.onSuccess { list ->
                products.value = list
            }
            result.onFailure { error ->
                message.value = error.message
            }

            isLoading.value = false
        }
    }

    fun fetchProductsByCategory(categoryID: String) {
        viewModelScope.launch {
            isLoading.value = true
            message.value = null
            val result = productRepository.getProductsByCategory(categoryID)

            result.onSuccess { list ->
                products.value = list
            }

            result.onFailure { error ->
                message.value = error.message
            }

            isLoading.value = false
        }
    }
}