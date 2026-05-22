package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import ltdd.dacsba.groceries.data.model.PaymentQrConfig
import java.text.NumberFormat
import java.util.Locale

// ─── Màu sắc riêng cho màn hình thanh toán ────────────────────────────────────
private val PayGreen = Color(0xFF22C55E)
private val PayGreenLight = Color(0xFFDCFCE7)
private val PayBlue = Color(0xFF3B82F6)
private val PayBlueLight = Color(0xFFEFF6FF)
private val PayOrange = AccentOrange   // Dùng lại từ BuyerHome.kt
private val BgGray = Color(0xFFF8FAFC)
private val TextPrimary = DarkNavy     // Dùng lại từ BuyerHome.kt
private val TextSecondary = Color(0xFF64748B)
private val DividerColor = Color(0xFFE2E8F0)
private val CardBg = Color.White

/**
 * Màn hình thanh toán VietQR.
 *
 * Luồng:
 * 1. Hiển thị mã QR động (Coil AsyncImage từ VietQR.io API – không cần lib QR)
 * 2. Đếm ngược 60s → Tự ghi SUCCESS vào Firestore (giả lập webhook)
 * 3. addSnapshotListener bắt được → Tự động chuyển sang màn hình SUCCESS
 * 4. Nút "Đã chuyển khoản" → Bấm bất kỳ lúc nào để xác nhận ngay lập tức
 *
 * @param orderId ID đơn hàng
 * @param amount  Tổng tiền cần thanh toán (VND)
 * @param navController Navigation controller
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQrScreen(
    orderId: String,
    amount: Long,
    sellerId: String,
    navController: NavController,
    viewModel: PaymentViewModel = viewModel()
) {
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val countdown by viewModel.countdownSeconds.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()
    val config by viewModel.qrConfig.collectAsState()

    val addInfo = "DONHANG ${orderId.takeLast(8).uppercase()}"
    val qrUrl = config.buildQrImageUrl(amount, addInfo)

    // Load thông tin seller rồi khởi động listener + countdown
    LaunchedEffect(orderId, sellerId) {
        viewModel.loadSellerConfig(sellerId)   // fetch tên + sinh STK theo seller
        viewModel.startListening(orderId)
        viewModel.startCountdown()
    }

    // Khi SUCCESS → chờ animation 2s rồi navigate về Orders
    LaunchedEffect(paymentStatus) {
        if (paymentStatus == PaymentStatus.SUCCESS) {
            delay(2200)
            navController.navigate(BuyerRoutes.ORDERS) {
                popUpTo(BuyerRoutes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thanh toán",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgGray
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Màn hình LOADING (chờ fetch thông tin seller) ──────────────────────
            AnimatedVisibility(
                visible = paymentStatus == PaymentStatus.LOADING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = PayBlue, modifier = Modifier.size(48.dp))
                        Text("Đang tải thông tin thanh toán...", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
            // ── Màn hình WAITING (QR + countdown) ─────────────────────────────
            AnimatedVisibility(
                visible = paymentStatus == PaymentStatus.WAITING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                WaitingPaymentContent(
                    qrUrl = qrUrl,
                    config = config,
                    amount = amount,
                    addInfo = addInfo,
                    countdown = countdown,
                    isConfirming = isConfirming,
                    onConfirm = { viewModel.confirmManually() }
                )
            }

            // ── Màn hình SUCCESS (animation checkmark) ────────────────────────
            AnimatedVisibility(
                visible = paymentStatus == PaymentStatus.SUCCESS,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut()
            ) {
                PaymentSuccessContent(amount = amount, orderId = orderId)
            }
        }
    }
}

// ─── Nội dung chính: Màn hình chờ thanh toán ──────────────────────────────────
@Composable
private fun WaitingPaymentContent(
    qrUrl: String,
    config: PaymentQrConfig,
    amount: Long,
    addInfo: String,
    countdown: Int,
    isConfirming: Boolean,
    onConfirm: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val formattedAmount = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

    // Progress cho vòng tròn đếm ngược
    val progress = countdown.toFloat() / PaymentViewModel.COUNTDOWN_SECONDS.toFloat()
    val countdownColor = when {
        countdown > 30 -> PayGreen
        countdown > 10 -> PayOrange
        else -> Color(0xFFEF4444)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Card QR chính ──────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header logo VietQR giả lập
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1565C0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("V", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Text(
                        "VietQR",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("✦", color = Color(0xFFE53935), fontSize = 16.sp)
                    Text(
                        "MB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFE53935)
                    )
                }

                HorizontalDivider(color = DividerColor)

                // Ảnh QR tải từ VietQR.io API qua Coil (không cần thư viện QR)
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(qrUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Mã QR thanh toán VietQR",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, DividerColor, RoundedCornerShape(12.dp)),
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = PayBlue, modifier = Modifier.size(36.dp))
                                Text("Đang tải mã QR...", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️", fontSize = 32.sp)
                                Text("Không tải được QR", fontSize = 12.sp, color = Color(0xFFEF4444))
                                Text("Kiểm tra kết nối mạng", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                )

                // ── Thông tin tài khoản ──
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccountInfoRow(
                            label = "Số tài khoản",
                            value = config.accountNo,
                            copyable = true,
                            onCopy = { clipboard.setText(AnnotatedString(config.accountNo)) }
                        )
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        AccountInfoRow(label = "Chủ tài khoản", value = config.accountName)
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        AccountInfoRow(label = "Ngân hàng", value = "MBBank")
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        AccountInfoRow(
                            label = "Nội dung",
                            value = addInfo,
                            valueColor = Color(0xFFE53935),
                            copyable = true,
                            onCopy = { clipboard.setText(AnnotatedString(addInfo)) }
                        )
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        AccountInfoRow(
                            label = "Số tiền",
                            value = "${formattedAmount}đ",
                            valueColor = PayOrange,
                            boldValue = true
                        )
                    }
                }
            }
        }

        // ── Card đếm ngược + nút xác nhận ─────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vòng tròn đếm ngược
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(80.dp),
                        color = countdownColor,
                        strokeWidth = 6.dp,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = countdown.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = countdownColor
                        )
                        Text("giây", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                // Mô tả trạng thái
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Chấm nhấp nháy
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PayGreen.copy(alpha = alpha), CircleShape)
                    )
                    Text(
                        "Đang chờ xác nhận thanh toán...",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "Hệ thống sẽ tự động xác nhận sau khi hết giờ đếm ngược",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = DividerColor)

                // Nút xác nhận thủ công
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayGreen,
                        disabledContainerColor = PayGreen.copy(alpha = 0.5f)
                    ),
                    enabled = !isConfirming
                ) {
                    if (isConfirming) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Đang xác nhận...", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Text(
                            "✓  Đã chuyển khoản",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Bấm nút trên nếu bạn đã chuyển khoản xong và muốn xác nhận ngay",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Màn hình thành công ───────────────────────────────────────────────────────
@Composable
private fun PaymentSuccessContent(amount: Long, orderId: String) {
    val formattedAmount = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

    // Animation checkmark phóng to
    val scaleAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Hiệu ứng vòng tròn xung
    val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icon check với gradient nền
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(PayGreenLight, Color(0xFFBBF7D0))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Thành công",
                    tint = PayGreen,
                    modifier = Modifier
                        .size(90.dp)
                        .scale(scaleAnim.value)
                )
            }

            // Text thành công
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Thanh toán thành công!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${formattedAmount}đ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = PayGreen
                )
                Text(
                    text = "Đơn hàng #${orderId.takeLast(8).uppercase()}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Card thông tin
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PayGreenLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "✅ Hệ thống đã ghi nhận thanh toán",
                        fontSize = 14.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Đơn hàng của bạn sẽ sớm được xử lý",
                        fontSize = 13.sp,
                        color = Color(0xFF16A34A),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Loading chuyển trang
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = PayGreen,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    "Đang chuyển về đơn hàng...",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ─── Component: Một hàng thông tin tài khoản ──────────────────────────────────
@Composable
private fun AccountInfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    boldValue: Boolean = false,
    copyable: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = if (boldValue) FontWeight.Bold else FontWeight.SemiBold,
                color = valueColor,
                textAlign = TextAlign.End
            )
            if (copyable && onCopy != null) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Sao chép",
                        tint = PayBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
