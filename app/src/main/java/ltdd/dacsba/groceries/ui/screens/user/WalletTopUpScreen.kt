package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import ltdd.dacsba.groceries.data.model.PaymentQrConfig
import ltdd.dacsba.groceries.data.model.WalletTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WGreen = Color(0xFF22C55E)
private val WGreenLight = Color(0xFFDCFCE7)
private val WPurple = Color(0xFF787FF6)
private val WPurpleLight = Color(0xFFEEF0FF)
private val WBg = Color(0xFFF8FAFC)
private val WText = Color(0xFF1B2430)
private val WTextSub = Color(0xFF64748B)
private val WDivider = Color(0xFFE2E8F0)

// Các mức nạp tiền gợi ý
private val QUICK_AMOUNTS = listOf(50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WalletTopUpScreen(
    navController: NavController,
    viewModel: WalletViewModel = viewModel()
) {
    val walletBalance by viewModel.walletBalance
    val walletAccountNo by viewModel.walletAccountNo
    val walletAccountName by viewModel.walletAccountName
    val transactions by viewModel.transactions
    val isLoading by viewModel.isLoading
    val isTopping by viewModel.isTopping

    var amountInput by remember { mutableStateOf("") }
    var showQr by remember { mutableStateOf(false) }
    var qrAmount by remember { mutableStateOf(0L) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val formattedBalance = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(walletBalance)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ví tiền", fontWeight = FontWeight.Bold, color = WText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = WText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = WBg
    ) { padding ->
        if (showQr) {
            WalletQrContent(
                amount = qrAmount,
                userId = uid,
                accountNo = walletAccountNo,
                accountName = walletAccountName,
                isConfirming = isTopping,
                onConfirm = {
                    viewModel.topUp(
                        amount = qrAmount.toDouble(),
                        onSuccess = { newBalance ->
                            showQr = false
                            amountInput = ""
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Nạp thành công! Số dư: ${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(newBalance)}đ")
                            }
                        },
                        onError = { err ->
                            scope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    )
                },
                onCancel = { showQr = false },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header số dư ví
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(WPurple, Color(0xFF1CA7EC)))
                        )
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Số dư ví", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(28.dp).padding(top = 4.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "${formattedBalance}đ",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Card nạp tiền
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Nạp tiền vào ví", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WText)

                        // Nhập số tiền thủ công
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Nhập số tiền (VNĐ)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                if (amountInput.isNotEmpty()) {
                                    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amountInput.toLong())
                                    Text("${fmt}đ", fontSize = 12.sp, color = WPurple, fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(end = 8.dp))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WPurple,
                                unfocusedBorderColor = WDivider
                            )
                        )

                        // Gợi ý mức nạp nhanh
                        Text("Chọn nhanh:", fontSize = 13.sp, color = WTextSub)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QUICK_AMOUNTS.forEach { amt ->
                                val fmtAmt = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amt)
                                FilterChip(
                                    selected = amountInput == amt.toString(),
                                    onClick = { amountInput = amt.toString() },
                                    label = { Text("${fmtAmt}đ", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WPurpleLight,
                                        selectedLabelColor = WPurple
                                    )
                                )
                            }
                        }

                        // Nút tạo QR
                        Button(
                            onClick = {
                                val amt = amountInput.toLongOrNull() ?: 0L
                                if (amt < 10_000) {
                                    scope.launch { snackbarHostState.showSnackbar("Số tiền tối thiểu là 10,000đ") }
                                    return@Button
                                }
                                qrAmount = amt
                                showQr = true
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WPurple),
                            enabled = amountInput.isNotEmpty()
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tạo mã QR nạp tiền", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Lịch sử giao dịch
                if (transactions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Lịch sử giao dịch", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WText)
                            Spacer(Modifier.height(12.dp))
                            transactions.forEach { tx ->
                                WalletTxRow(tx)
                                if (tx != transactions.last()) {
                                    HorizontalDivider(color = WDivider, modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun WalletQrContent(
    amount: Long,
    userId: String,
    accountNo: String,
    accountName: String,
    isConfirming: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addInfo = "NAPTIEN ${userId.takeLast(8).uppercase()}"
    // Dùng accountNo riêng của user trong QR config
    val qrConfig = PaymentQrConfig.DEFAULT.copy(
        accountNo = accountNo.ifBlank { PaymentQrConfig.DEFAULT.accountNo },
        accountName = accountName.ifBlank { PaymentQrConfig.DEFAULT.accountName }
    )
    val qrUrl = qrConfig.buildQrImageUrl(amount, addInfo)
    val formattedAmount = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // QR Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Quét mã để nạp tiền vào ví", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WText)
                Text(
                    "${formattedAmount}đ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WPurple
                )

                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(qrUrl).crossfade(true).build(),
                    contentDescription = "Mã QR nạp tiền",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, WDivider, RoundedCornerShape(12.dp)),
                    loading = {
                        Box(
                            Modifier.size(220.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = WPurple, modifier = Modifier.size(36.dp))
                        }
                    }
                )

                Surface(color = WBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WalletQrInfoRow("Ngân hàng", "MBBank")
                        HorizontalDivider(color = WDivider, thickness = 0.5.dp)
                        WalletQrInfoRow("Số tài khoản", qrConfig.accountNo, copyable = true) {
                            clipboard.setText(AnnotatedString(qrConfig.accountNo))
                        }
                        HorizontalDivider(color = WDivider, thickness = 0.5.dp)
                        WalletQrInfoRow("Chủ tài khoản", qrConfig.accountName)
                        HorizontalDivider(color = WDivider, thickness = 0.5.dp)
                        WalletQrInfoRow("Nội dung CK", addInfo, valueColor = Color(0xFFE53935), copyable = true) {
                            clipboard.setText(AnnotatedString(addInfo))
                        }
                        HorizontalDivider(color = WDivider, thickness = 0.5.dp)
                        WalletQrInfoRow("Số tiền", "${formattedAmount}đ", valueColor = WPurple, boldValue = true)
                    }
                }
            }
        }

        // Nút xác nhận đã chuyển khoản
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WGreen),
            enabled = !isConfirming
        ) {
            if (isConfirming) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Đang xác nhận...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Đã chuyển khoản – Xác nhận nạp", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WTextSub)
        ) {
            Text("Huỷ", fontWeight = FontWeight.Medium)
        }

        Text(
            "Sau khi chuyển khoản đúng nội dung, bấm xác nhận để cộng tiền vào ví.",
            fontSize = 12.sp,
            color = WTextSub,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun WalletQrInfoRow(
    label: String,
    value: String,
    valueColor: Color = WText,
    boldValue: Boolean = false,
    copyable: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = WTextSub, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                value, fontSize = 13.sp,
                fontWeight = if (boldValue) FontWeight.Bold else FontWeight.SemiBold,
                color = valueColor
            )
            if (copyable && onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun WalletTxRow(tx: WalletTransaction) {
    val isTopUp = tx.type == "TOPUP"
    val fmtAmount = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(tx.amount)
    val fmtDate = remember(tx.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = CircleShape, color = if (isTopUp) WGreenLight else WPurpleLight, modifier = Modifier.size(38.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isTopUp) Icons.Default.Add else Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = if (isTopUp) WGreen else WPurple,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.note.ifBlank { if (isTopUp) "Nạp tiền" else "Thanh toán" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WText)
            Text(fmtDate, fontSize = 11.sp, color = WTextSub)
        }
        Text(
            text = "${if (isTopUp) "+" else "-"}${fmtAmount}đ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTopUp) WGreen else Color(0xFFEF4444)
        )
    }
}
