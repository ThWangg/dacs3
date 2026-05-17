package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ltdd.dacsba.groceries.data.model.Product
import java.text.NumberFormat
import java.util.Locale

// ===================== Screen (kết nối ViewModel) =====================

@Composable
fun SellerStatsScreen(
    viewModel: SellerStatsViewModel = viewModel()
) {
    val selectedPeriod by viewModel.selectedPeriod
    val revenueByDay by viewModel.revenueByDay
    val topSellingProducts by viewModel.topSellingProducts
    val totalRevenue by viewModel.totalRevenue
    val totalOrderCount by viewModel.totalOrderCount
    val isLoading by viewModel.isLoading

    SellerStatsContent(
        selectedPeriod = selectedPeriod,
        revenueByDay = revenueByDay,
        topSellingProducts = topSellingProducts,
        totalRevenue = totalRevenue,
        totalOrderCount = totalOrderCount,
        isLoading = isLoading,
        onPeriodSelected = { viewModel.onPeriodSelected(it) },
        onRefresh = { viewModel.loadStats() }
    )
}

// ===================== UI =====================

@Composable
fun SellerStatsContent(
    selectedPeriod: SellerStatsViewModel.StatsPeriod,
    revenueByDay: List<Pair<String, Double>>,
    topSellingProducts: List<Product>,
    totalRevenue: Double,
    totalOrderCount: Int,
    isLoading: Boolean,
    onPeriodSelected: (SellerStatsViewModel.StatsPeriod) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Thống kê",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Doanh thu & sản phẩm",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Làm mới",
                    tint = Color.Gray
                )
            }
        }

        // Bộ lọc thời gian
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SellerStatsViewModel.StatsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7CB342),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
            // Thẻ tổng quan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng doanh thu",
                    value = formatCurrencyShort(totalRevenue),
                    icon = Icons.Default.BarChart,
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Số đơn hàng",
                    value = totalOrderCount.toString(),
                    icon = Icons.Default.ShoppingCart,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Biểu đồ doanh thu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Biểu đồ doanh thu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (revenueByDay.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có dữ liệu doanh thu", color = Color.Gray)
                        }
                    } else {
                        RevenueBarChart(
                            revenueData = revenueByDay,
                            barColor = Color(0xFF7CB342),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top sản phẩm bán chạy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Top sản phẩm bán chạy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (topSellingProducts.isEmpty()) {
                        Text(
                            text = "Chưa có sản phẩm nào",
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        topSellingProducts.forEachIndexed { index, product ->
                            TopProductItem(
                                rank = index + 1,
                                product = product
                            )
                            if (index < topSellingProducts.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Biểu đồ cột doanh thu vẽ bằng Compose Canvas
@Composable
fun RevenueBarChart(
    revenueData: List<Pair<String, Double>>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maximumRevenue = revenueData.maxOfOrNull { it.second } ?: 1.0

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val bottomLabelHeight = 30f
        val chartHeight = canvasHeight - bottomLabelHeight
        val barCount = revenueData.size
        val barWidth = (canvasWidth / barCount) * 0.6f
        val barSpacing = canvasWidth / barCount

        revenueData.forEachIndexed { index, (label, revenue) ->
            val barHeightRatio = (revenue / maximumRevenue).toFloat()
            val barHeightPx = chartHeight * barHeightRatio
            val barLeft = index * barSpacing + (barSpacing - barWidth) / 2
            val barTop = chartHeight - barHeightPx

            // Vẽ cột
            drawRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeightPx)
            )

            // Nhãn ngày bên dưới
            drawContext.canvas.nativeCanvas.drawText(
                label,
                barLeft + barWidth / 2,
                canvasHeight,
                android.graphics.Paint().apply {
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.GRAY
                }
            )
        }
    }
}

@Composable
fun TopProductItem(
    rank: Int,
    product: Product
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Huy hiệu thứ hạng
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> Color(0xFFE0E0E0)
                    },
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.White else Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = "Đã bán: ${product.soldCount} ${product.unit}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            text = formatCurrencyShort(product.price),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7CB342)
        )
    }
}

private fun formatCurrencyShort(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "${String.format("%.1f", amount / 1_000_000)}M"
        amount >= 1_000 -> "${String.format("%.0f", amount / 1_000)}K"
        else -> "${amount.toInt()}đ"
    }
}

// ===================== Preview =====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerStatsPreview() {
    val mockProducts = listOf(
        Product(id = "1", name = "Táo Mỹ", soldCount = 150, price = 75000.0, unit = "kg"),
        Product(id = "2", name = "Rau muống", soldCount = 120, price = 15000.0, unit = "bó"),
        Product(id = "3", name = "Thịt heo", soldCount = 80, price = 120000.0, unit = "kg"),
        Product(id = "4", name = "Trứng gà", soldCount = 60, price = 30000.0, unit = "vỉ"),
        Product(id = "5", name = "Chuối", soldCount = 40, price = 25000.0, unit = "nải")
    )
    val mockRevenueData = listOf(
        "10/05" to 450000.0,
        "11/05" to 320000.0,
        "12/05" to 680000.0,
        "13/05" to 290000.0,
        "14/05" to 510000.0,
        "15/05" to 740000.0,
        "16/05" to 380000.0
    )

    SellerStatsContent(
        selectedPeriod = SellerStatsViewModel.StatsPeriod.WEEK,
        revenueByDay = mockRevenueData,
        topSellingProducts = mockProducts,
        totalRevenue = 3370000.0,
        totalOrderCount = 47,
        isLoading = false,
        onPeriodSelected = {},
        onRefresh = {}
    )
}
