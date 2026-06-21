package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private val SuccessGreen = Color(0xFF22C55E)
private val SuccessGreenLight = Color(0xFFDCFCE7)
private val SuccessText = Color(0xFF1B2430)
private val SuccessTextSub = Color(0xFF64748B)

@Composable
fun WalletPaymentSuccessScreen(
    orderId: String,
    amount: Long,
    navController: NavController
) {
    val formattedAmount = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

    val scaleAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        delay(2500)
        navController.navigate(BuyerRoutes.ORDERS) {
            popUpTo(BuyerRoutes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icon check lớn
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(listOf(SuccessGreenLight, Color(0xFFBBF7D0))),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier
                        .size(90.dp)
                        .scale(scaleAnim.value)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Đặt hàng thành công!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessText,
                    textAlign = TextAlign.Center
                )
                Text(
                    "${formattedAmount}đ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessGreen
                )
                Text(
                    "Đơn hàng #${orderId.takeLast(8).uppercase()}",
                    fontSize = 14.sp,
                    color = SuccessTextSub,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Thẻ thông tin thanh toán
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "💳  Thanh toán bằng Ví",
                        fontSize = 14.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Tiền đã được trừ khỏi ví của bạn",
                        fontSize = 13.sp,
                        color = Color(0xFF16A34A),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Đơn hàng sẽ sớm được người bán xử lý",
                        fontSize = 12.sp,
                        color = Color(0xFF15803D),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = SuccessGreen,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    "Đang chuyển về đơn hàng...",
                    fontSize = 13.sp,
                    color = SuccessTextSub
                )
            }
        }
    }
}
