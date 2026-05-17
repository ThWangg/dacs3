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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.User

// Bộ màu Admin
private val AdminPrimary = Color(0xFF787FF6)
private val AdminSecondary = Color(0xFF1CA7EC)
private val AdminDark = Color(0xFF1F2F98)

// ===================== Screen (kết nối ViewModel) =====================

@Composable
fun AdminUserScreen(
    viewModel: AdminUserViewModel = viewModel()
) {
    val context = LocalContext.current
    val filteredUsers by viewModel.filteredUsers
    val searchQuery by viewModel.searchQuery
    val selectedRoleFilter by viewModel.selectedRoleFilter
    val isLoading by viewModel.isLoading
    val actionMessage by viewModel.actionMessage

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    AdminUserContent(
        users = filteredUsers,
        searchQuery = searchQuery,
        selectedRoleFilter = selectedRoleFilter,
        isLoading = isLoading,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onRoleFilterSelected = { viewModel.onRoleFilterSelected(it) },
        onLockAccount = { viewModel.lockAccount(it) },
        onUnlockAccount = { viewModel.unlockAccount(it) }
    )
}

// ===================== UI =====================

@Composable
fun AdminUserContent(
    users: List<User>,
    searchQuery: String,
    selectedRoleFilter: String?,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onRoleFilterSelected: (String?) -> Unit,
    onLockAccount: (String) -> Unit,
    onUnlockAccount: (String) -> Unit
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
                        colors = listOf(AdminDark, AdminPrimary)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Quản lý người dùng",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${users.size} người dùng",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Tìm tên hoặc email...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AdminPrimary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminPrimary,
                unfocusedBorderColor = Color(0xFFD0D3FF)
            )
        )

        // Bộ lọc role
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedRoleFilter == null,
                    onClick = { onRoleFilterSelected(null) },
                    label = { Text("Tất cả", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedRoleFilter == AppConstant.Roles.BUYER,
                    onClick = { onRoleFilterSelected(AppConstant.Roles.BUYER) },
                    label = { Text("Người mua", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedRoleFilter == AppConstant.Roles.SELLER,
                    onClick = { onRoleFilterSelected(AppConstant.Roles.SELLER) },
                    label = { Text("Người bán", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AdminPrimary
            )
        }

        if (users.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không tìm thấy người dùng", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users) { user ->
                    UserManagementCard(
                        user = user,
                        onLockAccount = onLockAccount,
                        onUnlockAccount = onUnlockAccount
                    )
                }
            }
        }
    }
}

@Composable
fun UserManagementCard(
    user: User,
    onLockAccount: (String) -> Unit,
    onUnlockAccount: (String) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLockAction by remember { mutableStateOf(true) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (isLockAction) "Khóa tài khoản?" else "Mở khóa tài khoản?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isLockAction) {
                        "Tài khoản \"${user.username}\" sẽ bị vô hiệu hóa và không thể đăng nhập."
                    } else {
                        "Tài khoản \"${user.username}\" sẽ được khôi phục quyền truy cập."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        if (isLockAction) onLockAccount(user.uid)
                        else onUnlockAccount(user.uid)
                    }
                ) {
                    Text(
                        text = "Xác nhận",
                        color = if (isLockAction) Color.Red else AdminPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isDeactivated) Color(0xFFFFF3F3) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = when (user.role) {
                    AppConstant.Roles.ADMIN -> Color(0xFFE8F5E9)
                    AppConstant.Roles.SELLER -> Color(0xFFE3F2FD)
                    else -> Color(0xFFF3E5F5)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.username.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (user.role) {
                            AppConstant.Roles.ADMIN -> Color(0xFF2E7D32)
                            AppConstant.Roles.SELLER -> Color(0xFF1565C0)
                            else -> Color(0xFF7B1FA2)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username.ifBlank { "Không rõ" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (user.role) {
                            AppConstant.Roles.ADMIN -> Color(0xFFE8F5E9)
                            AppConstant.Roles.SELLER -> Color(0xFFE3F2FD)
                            else -> Color(0xFFF3E5F5)
                        }
                    ) {
                        Text(
                            text = when (user.role) {
                                AppConstant.Roles.ADMIN -> "Admin"
                                AppConstant.Roles.SELLER -> "Seller"
                                else -> "Buyer"
                            },
                            fontSize = 10.sp,
                            color = when (user.role) {
                                AppConstant.Roles.ADMIN -> Color(0xFF2E7D32)
                                AppConstant.Roles.SELLER -> Color(0xFF1565C0)
                                else -> Color(0xFF7B1FA2)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (user.isDeactivated) {
                    Text(
                        text = "⚠ Tài khoản bị khóa",
                        fontSize = 11.sp,
                        color = Color.Red
                    )
                }
            }

            // Nút khóa/mở khóa
            IconButton(
                onClick = {
                    isLockAction = !user.isDeactivated
                    showConfirmDialog = true
                }
            ) {
                Icon(
                    imageVector = if (user.isDeactivated) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = if (user.isDeactivated) "Mở khóa" else "Khóa",
                    tint = if (user.isDeactivated) AdminSecondary else Color.Red,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ===================== Preview =====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminUserScreenPreview() {
    val mockUsers = listOf(
        User(uid = "1", username = "Nguyễn Văn A", email = "a@gmail.com", role = "BUYER"),
        User(uid = "2", username = "Trần Thị B", email = "b@gmail.com", role = "SELLER", isDeactivated = false),
        User(uid = "3", username = "Lê Văn C", email = "c@gmail.com", role = "BUYER", isDeactivated = true)
    )
    AdminUserContent(
        users = mockUsers,
        searchQuery = "",
        selectedRoleFilter = null,
        isLoading = false,
        onSearchQueryChange = {},
        onRoleFilterSelected = {},
        onLockAccount = {},
        onUnlockAccount = {}
    )
}
