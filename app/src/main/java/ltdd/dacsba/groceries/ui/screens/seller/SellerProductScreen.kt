package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ltdd.dacsba.groceries.data.model.Product

@Composable
fun SellerProductScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val products by viewModel.products
    val isLoading by viewModel.isLoading

    SellerProductContent(
        products = products,
        isLoading = isLoading,
        onAddClick = {
            navController.navigate(ltdd.dacsba.groceries.data.constant.AppConstant.Routes.SELLER_ADD_PRODUCT)
        },
        onEditClick = { product ->
            navController.navigate("${ltdd.dacsba.groceries.data.constant.AppConstant.Routes.SELLER_EDIT_PRODUCT}/${product.id}")
        },
        onDeleteClick = { productId ->
            viewModel.deleteProduct(productId)
        }
    )
}

@Composable
fun SellerProductContent(
    products: List<Product>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Products",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults
                    .buttonColors(
                        containerColor = Color(0xFF7CB342)
                    ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp))

                Spacer(Modifier.width(4.dp))

                Text("Add", fontSize = 14.sp)
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF7CB342)
            )
        }

        if (products.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No products found", color = Color.Gray)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductItemRow(
                    product = product,
                    onEdit = { onEditClick(product) },
                    onDelete = { onDeleteClick(product.id) }
                )
            }
        }
    }
}

@Composable
fun ProductItemRow(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {

                Icon(Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = "${product.price}đ / ${product.unit}",
                    color = Color(0xFF7CB342),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Stock: ${product.stock} | Sold: ${product.soldCount}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerProductPreview() {
    val mockList = listOf(
        Product(id="1", name="Apple", price=50000.0, unit="kg", stock=100, soldCount = 10),
        Product(id="2", name="Banana", price=20000.0, unit="kg", stock=50, soldCount = 5)
    )
    SellerProductContent(
        products = mockList,
        isLoading = false,
        onAddClick = {},
        onEditClick = {},
        onDeleteClick = {}
    )
}