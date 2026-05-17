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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import ltdd.dacsba.groceries.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AdminPrimary = Color(0xFF787FF6)
private val AdminSecondary = Color(0xFF1CA7EC)
private val AdminDark = Color(0xFF1F2F98)

// ===================== Screen (kết nối ViewModel) =====================

@Composable
fun AdminShopApprovalScreen(
    viewModel: AdminShopApprovalViewModel = viewModel()
) {
    val context = LocalContext.current
    val pendingSellerList by viewModel.pendingSellerList
    val isLoading by viewModel.isLoading
    val actionMessage by viewModel.actionMessage

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    ShopApprovalContent(
        pendingSellerList = pendingSellerList,
        isLoading = isLoading,
        onApprove = { viewModel.approveShop(it) },
        onReject = { viewModel.rejectShop(it) }
    )
}

// ===================== UI =====================

@Composable
fun ShopApprovalContent(
    pendingSellerList: List<User>,
    isLoading: Boolean,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FF))
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AdminDark, AdminSecondary)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Kiểm duyệt gian hàng",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${pendingSellerList.size} yêu cầu đang chờ",
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

        if (pendingSellerList.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không có yêu cầu nào đang chờ duyệt",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingSellerList) { user ->
                    ShopApprovalCard(
                        user = user,
                        onApprove = onApprove,
                        onReject = onReject
                    )
                }
            }
        }
    }
}

@Composable
fun ShopApprovalCard(
    user: User,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar gian hàng
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFE8EAFD)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = AdminPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username.ifBlank { "Không rõ" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // Badge PENDING
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF3CD)
                ) {
                    Text(
                        text = "Chờ duyệt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Thông tin gian hàng
            InfoRow(label = "Tên gian hàng", value = user.shopName.ifBlank { "Chưa đặt tên" })
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(label = "Số điện thoại", value = user.phone.ifBlank { "Chưa cung cấp" })
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(
                label = "Ngày đăng ký",
                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(user.createdAt))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nút hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onReject(user.uid) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Từ chối", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onApprove(user.uid) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duyệt", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
    }
}

// ===================== Preview =====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShopApprovalPreview() {
    val mockUsers = listOf(
        User(
            uid = "1",
            username = "Nguyễn Văn An",
            email = "an@gmail.com",
            shopName = "Shop Rau Sạch An",
            phone = "0901234567",
            sellerStatus = "PENDING",
            createdAt = System.currentTimeMillis() - 86400000L
        ),
        User(
            uid = "2",
            username = "Trần Thị Bình",
            email = "binh@gmail.com",
            shopName = "Hoa Quả Bình",
            phone = "0987654321",
            sellerStatus = "PENDING",
            createdAt = System.currentTimeMillis() - 3600000L
        )
    )
    ShopApprovalContent(
        pendingSellerList = mockUsers,
        isLoading = false,
        onApprove = {},
        onReject = {}
    )
}
