package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SellerDashboardScreen(
    navController: NavController,
    viewModel: SellerViewModel = viewModel()
) {
    val productCount by viewModel.totalProducts
    val totalSold by viewModel.totalSold
    val avgRating by viewModel.avgRating
    val isLoading by viewModel.isLoading

    SellerDashboardContent(
        productCount = productCount,
        totalSold = totalSold,
        avgRating = avgRating,
        isLoading = isLoading,
        onRefresh = { viewModel.refreshData() }
    )
}

@Composable
fun SellerDashboardContent(
    productCount: Int,
    totalSold: Int,
    avgRating: Double,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        //header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dashboard",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = "Your status",
                    color = Color.Gray
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7CB342))
            }
        } else {
            //hanfg 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Product",
                    value = productCount.toString(),
                    icon = Icons.Default.ShoppingCart,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Sold",
                    value = totalSold.toString(),
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            //hàng 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Rating",
                    value = "%.1f ★".format(avgRating),
                    icon = Icons.Default.Star,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Stock",
                    value = "99+",
                    icon = Icons.AutoMirrored.Filled.List,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF7B1FA2)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recent activity",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("Order #12345") },
                    supportingContent = { Text("Waiting to confirm") },
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF9C4)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = Color(0xFFFBC02D)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults
            .cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerDashboardPreview() {
    SellerDashboardContent(
        productCount = 12,
        totalSold = 450,
        avgRating = 4.7,
        isLoading = false,
        onRefresh = {}
    )
}