package ltdd.dacsba.groceries.ui.screens.seller

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ltdd.dacsba.groceries.ui.components.AppTextField

@Composable
fun SellerProfileScreen(
    navController: NavController,
    viewModel: SellerProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading
    val isEditMode by viewModel.isEditMode

    androidx.compose.runtime.LaunchedEffect(uiState.updateMessage) {
        uiState.updateMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUpdateMessage()
        }
    }

    SellerProfileContent(
        username = uiState.username,
        email = uiState.email,
        shopName = uiState.shopName,
        phone = uiState.phone,
        isLoading = isLoading,
        isEditMode = isEditMode,
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onShopNameChange = { viewModel.onShopNameChange(it) },
        onPhoneChange = { viewModel.onPhoneChange(it) },
        onEditClick = { viewModel.enterEditMode() },
        onSaveClick = { viewModel.saveProfile() },
        onCancelEdit = { viewModel.cancelEdit() },
        onLogoutClick = {
            viewModel.logout()
            navController.navigate(ltdd.dacsba.groceries.data.constant.AppConstant.Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}

@Composable
fun SellerProfileContent(
    username: String,
    email: String,
    shopName: String,
    phone: String,
    isLoading: Boolean,
    isEditMode: Boolean,
    onUsernameChange: (String) -> Unit,
    onShopNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Confirm ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
            .verticalScroll(rememberScrollState())
    ) {

        //header + avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7CB342))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //avartar
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF7CB342),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = username.ifBlank { "Seller" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        if (!isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF7CB342)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (isEditMode) {
                    AppTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = "Username",
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppTextField(
                        value = shopName,
                        onValueChange = onShopNameChange,
                        label = "Shop name",
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        label = "Phone",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {

                    ProfileInfoRow(
                        label = "Username",
                        value = username.ifBlank { "--" }
                    )
                    ProfileInfoRow(
                        label = "Shop name",
                        value = shopName.ifBlank { "--" }
                    )
                    ProfileInfoRow(
                        label = "Phone",
                        value = phone.ifBlank { "--" }
                    )
                    ProfileInfoRow(
                        label = "Email",
                        value = email.ifBlank { "--" }
                    )
                }
            }
        }

        //nút lưu/huỷ
        if (isEditMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7CB342)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Save changes")
                    }
                }

                OutlinedButton(
                    onClick = onCancelEdit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //logout buton
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SellerProfilePreview() {
    SellerProfileContent(
        username = "Nguyễn Văn A",
        email = "seller@gmail.com",
        shopName = "Shop Rau Sạch Xanh",
        phone = "0901234567",
        isLoading = false,
        isEditMode = false,
        onUsernameChange = {},
        onShopNameChange = {},
        onPhoneChange = {},
        onEditClick = {},
        onSaveClick = {},
        onCancelEdit = {},
        onLogoutClick = {}
    )
}
