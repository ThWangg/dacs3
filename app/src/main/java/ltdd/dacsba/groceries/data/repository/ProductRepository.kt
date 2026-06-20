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

            val products = snapshot.toObjects(Product::class.java).filter { it.status == "APPROVED" }
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

            val products = snapshot.toObjects(Product::class.java).filter { it.status == "APPROVED" }
            Result.success(products)
        }catch(e: Exception){
            Result.failure(e)
        }
    }

    suspend fun getProductsByTags(tags: List<String>): Result<List<Product>> {
        return try {
            if (tags.isEmpty()) return Result.success(emptyList())

val limitedTags = tags.take(10)
            
            val snapshot = productCollection
                .whereArrayContainsAny("tags", limitedTags)
                .get()
                .await()

            val products = snapshot.toObjects(Product::class.java).filter { it.status == "APPROVED" }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val snapshot = productCollection.get().await()
            val allProducts = snapshot.toObjects(Product::class.java)

val filteredProducts = allProducts.filter { product ->
                product.status == "APPROVED" && product.name.contains(query, ignoreCase = true)
            }
            Result.success(filteredProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(product: Product): Result<Boolean> {
        return try {
            productCollection.document(product.id).set(product).await()
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
            val docRef = productCollection.document(productId)
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.delete().await()
            } else {
                val snapshot = productCollection.whereEqualTo("id", productId).get().await()
                for (d in snapshot.documents) {
                    d.reference.delete().await()
                }
            }
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

    suspend fun getPendingProducts(): Result<List<Product>> {
        return try {
            val snapshot = productCollection
                .whereEqualTo("status", "PENDING")
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProductStatus(productId: String, status: String): Result<Boolean> {
        return try {
            val docRef = productCollection.document(productId)
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update("status", status).await()
            } else {
                val snapshot = productCollection.whereEqualTo("id", productId).get().await()
                if (!snapshot.isEmpty) {
                    for (d in snapshot.documents) {
                        d.reference.update("status", status).await()
                    }
                } else {
                    return Result.failure(Exception("Không tìm thấy sản phẩm trong Database"))
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}