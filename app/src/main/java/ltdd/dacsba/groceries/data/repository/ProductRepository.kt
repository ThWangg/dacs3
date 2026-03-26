package ltdd.dacsba.groceries.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.Product


class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productCollection = db.collection(AppConstant.COLLECTION_PRODUCTS)

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = productCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(categoryID: String): Result<List<Product>> {
        return try {
            val snapshot = productCollection
                .whereEqualTo("categoryId", categoryID)
                .get()
                .await()

            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        }catch(e: Exception){
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val snapshot = productCollection.get().await()
            val allProducts = snapshot.toObjects(Product::class.java)

            //filter
            val filteredProducts = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
            Result.success(filteredProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(product: Product): Result<Boolean> {
        return try {
            productCollection.add(product).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(productId: String): Result<Boolean> {
        return try{
            if(productId.isEmpty()) {
                return Result.failure(Exception("Invalid product ID"))
            }
            productCollection
                .document(productId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Boolean> {
        return try {
            if(product.id.isEmpty()) {
                return Result.failure(Exception("Invalid product ID"))
            }
            productCollection
                .document(product.id)
                .set(product)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //dashboard seller

    suspend fun getSellerProductsCount(sellerId: String): Result<List<Product>> {
        return try {
            val snapshot = productCollection
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)

            } catch (e: Exception) {
            Result.failure(e)
        }
    }
}