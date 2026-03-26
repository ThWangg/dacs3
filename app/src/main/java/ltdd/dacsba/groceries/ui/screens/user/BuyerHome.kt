package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ltdd.dacsba.groceries.R
import ltdd.dacsba.groceries.data.model.Product

val DarkNavy = Color(0xFF1B2430)
val AccentOrange = Color(0xFFFF7D4D)

@Composable
fun BuyerHomeScreen() {
    BuyerHomeContent()
}

@Composable
fun BuyerHomeContent() {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        BuyerHomeHeader()
        BuyerHomeBody()
    }
}

@Composable
fun BuyerHomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tuat_logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Taut Shop",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                )
            }

            //search icon
            Surface(
                modifier = Modifier.size(45.dp),
                shape = CircleShape,
                color = Color(0xFFF7F7F7),
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = { /* TODO: Search */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))
        CategorySection()
    }
}

@Composable
fun CategorySection() {
    val categories = listOf("Fruits", "Fast-food", "Vegetables", "Drinks")
    var selectedCategory by remember { mutableStateOf("Fruits") }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .clickable { selectedCategory = category },
                color = if (isSelected) DarkNavy else Color(0xFFF7F7F7)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = TextStyle(
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BuyerHomeBody() { val sampleProducts = listOf(
        Product(id="1", name="Apple", price=10.45, unit="kg", stock=55, imageUrl="https://img.freepik.com/free-photo/red-apples-isolated-white-background_1232-3122.jpg"),
        Product(id="2", name="Orange", price=14.75, unit="kg", stock=75, imageUrl="https://upload.wikimedia.org/wikipedia/commons/c/c4/Orange-Fruit-Pieces.jpg")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Text(
            text = "Popular Fruits",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sampleProducts) { product ->
                ProductCard(product = product, onClick = {})
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.name, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text(text = "${product.stock} cal", fontSize = 12.sp, color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$${product.price}/kg", fontWeight = FontWeight.Bold, color = AccentOrange)
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = AccentOrange
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BuyerHomeScreenPreview() {
    BuyerHomeScreen()
}