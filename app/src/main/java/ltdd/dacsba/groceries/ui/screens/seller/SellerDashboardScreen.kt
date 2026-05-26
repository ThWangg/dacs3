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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.remember
import ltdd.dacsba.groceries.data.model.SellerActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.width
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
    val totalStock by viewModel.totalStock
    val activities by viewModel.activities
    val isLoading by viewModel.isLoading

    SellerDashboardContent(
        productCount = productCount,
        totalSold = totalSold,
        avgRating = avgRating,
        totalStock = totalStock,
        activities = activities,
        isLoading = isLoading,
        onRefresh = { viewModel.refreshData() },
        onNotificationClick = { navController.navigate(SellerRoutes.NOTIFICATIONS) }
    )
}

@Composable
fun SellerDashboardContent(
    productCount: Int,
    totalSold: Int,
    avgRating: Double,
    totalStock: Int,
    activities: List<SellerActivity>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNotificationClick: () -> Unit
) {
    var visibleCount by remember { mutableStateOf(3) }

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
            Row {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.Gray)
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.Gray)
                }
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
                CircularProgressIndicator(color = SellerGreen)
            }
        } else {
            //hang 1
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
                    value = totalStock.toString(),
                    icon = Icons.Default.List,
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
        
        if (activities.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có hoạt động nào gần đây",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val displayedActivities = remember(activities, visibleCount) {
                activities.take(visibleCount)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    displayedActivities.forEachIndexed { index, activity ->
                        val icon = when (activity.type) {
                            "ADD_PRODUCT" -> Icons.Default.Add
                            "EDIT_PRODUCT" -> Icons.Default.Edit
                            "DELETE_PRODUCT" -> Icons.Default.Delete
                            "UPDATE_PROFILE" -> Icons.Default.Person
                            "UPDATE_AVATAR" -> Icons.Default.Person
                            "REMOVE_AVATAR" -> Icons.Default.Person
                            "ORDER_CONFIRM" -> Icons.Default.CheckCircle
                            "ORDER_SHIPPING" -> Icons.Default.List
                            "ORDER_DELIVERED" -> Icons.Default.CheckCircle
                            "ORDER_CANCEL" -> Icons.Default.Close
                            else -> Icons.Default.List
                        }

                        val tintColor = when (activity.type) {
                            "ADD_PRODUCT" -> Color(0xFF4CAF50)
                            "EDIT_PRODUCT" -> Color(0xFF2196F3)
                            "DELETE_PRODUCT" -> Color(0xFFF44336)
                            "UPDATE_PROFILE", "UPDATE_AVATAR", "REMOVE_AVATAR" -> Color(0xFF9C27B0)
                            "ORDER_CONFIRM", "ORDER_SHIPPING", "ORDER_DELIVERED" -> Color(0xFF4CAF50)
                            "ORDER_CANCEL" -> Color(0xFFF44336)
                            else -> Color.Gray
                        }

                        val bgColor = when (activity.type) {
                            "ADD_PRODUCT" -> Color(0xFFE8F5E9)
                            "EDIT_PRODUCT" -> Color(0xFFE3F2FD)
                            "DELETE_PRODUCT" -> Color(0xFFFFEBEE)
                            "UPDATE_PROFILE", "UPDATE_AVATAR", "REMOVE_AVATAR" -> Color(0xFFF3E5F5)
                            "ORDER_CONFIRM", "ORDER_SHIPPING", "ORDER_DELIVERED" -> Color(0xFFE8F5E9)
                            "ORDER_CANCEL" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFF5F5F5)
                        }

                        val formattedTime = remember(activity.timestamp) {
                            val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                            sdf.format(Date(activity.timestamp))
                        }

                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = activity.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ) 
                            },
                            supportingContent = { 
                                Column {
                                    Text(
                                        text = activity.message,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formattedTime,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = bgColor
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp),
                                        tint = tintColor
                                    )
                                }
                            }
                        )
                        if (index < displayedActivities.size - 1) {
                            androidx.compose.material3.HorizontalDivider(
                                color = Color(0xFFEEEEEE),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    val showMoreVisible = activities.size > visibleCount
                    val showLessVisible = visibleCount > 3

                    if (showMoreVisible || showLessVisible) {
                        androidx.compose.material3.HorizontalDivider(
                            color = Color(0xFFEEEEEE),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showMoreVisible) {
                                TextButton(
                                    onClick = { visibleCount += 3 }
                                ) {
                                    Text(
                                        text = "Xem thêm",
                                        color = SellerGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (showMoreVisible && showLessVisible) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            if (showLessVisible) {
                                TextButton(
                                    onClick = { visibleCount = 3 }
                                ) {
                                    Text(
                                        text = "Thu gọn",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
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
        totalStock = 1250,
        activities = emptyList(),
        isLoading = false,
        onRefresh = {},
        onNotificationClick = {}
    )
}