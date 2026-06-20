package ltdd.dacsba.groceries.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.R
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.data.model.User
import ltdd.dacsba.groceries.ui.components.AppTextField

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
){
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.message
    val loginSuccess by authViewModel.loginSuccess

    LaunchedEffect(loginSuccess) {
        loginSuccess?.let { user ->
            if (user.role == AppConstant.Roles.ADMIN) {
                navController.navigate(AppConstant.Routes.ADMIN_HOME)
            } else {
                navController.navigate(AppConstant.Routes.BUYER_HOME)
            }
        }
    }

    RegisterContent(
        username = username,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onUsernameChange = { username = it },
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onRegisterClick = { u, e, p, cp, r ->

            if(p == cp){
                val newUser = User(
                    username = u,
                    email = e,
                    role = r,

)

                authViewModel.register(newUser, p)
            }
            else{
                authViewModel.message.value = "Mật khẩu không khớp"
            }
        },
        onSignInClick = {
            navController.popBackStack()
        }
    )
}

@Composable
fun RegisterContent(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: (String, String, String, String, String) -> Unit,
    onSignInClick: () -> Unit
){
    var selectedRole by remember { mutableStateOf(AppConstant.Roles.BUYER) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ){
        Image(
            painter = painterResource(id = R.drawable.grocery_bag),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .rotate(-135f)
                .offset(x = (-15).dp, y = (200).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 15.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tuat_logo),
                    contentDescription = null,
                    modifier =  Modifier.size(80.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(height = 70.dp, width = 120.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.width(60.dp),
                        thickness = 4.dp,
                        color = Color(0xFF787FF6)
                    )
                    Text(
                        text = "TAUT Shop",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2F98)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tạo tài khoản mới",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Text(
                text = "Vui lòng điền thông tin",
                color = Color.Gray,
                fontSize = 18.sp
            )

            Column(
                modifier = Modifier
                    .padding(top = 20.dp)
            ){
                AppTextField(
                    value = username,
                    onValueChange = onUsernameChange ,
                    label = "Tên người dùng",
                    modifier = Modifier
                        .width(300.dp)
                )

                AppTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    modifier = Modifier.padding(top = 10.dp)
                )

                AppTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Mật khẩu",
                    isPassword = true,
                    modifier = Modifier.padding(top = 10.dp)
                )

                AppTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = "Xác nhận mật khẩu",
                    isPassword = true,
                    modifier = Modifier.padding(top = 10.dp)
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

Button(
                    onClick = {
                        onRegisterClick(username, email, password, confirmPassword, selectedRole)
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(180.dp)
                        .height(50.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF787FF6), Color(0xFF1CA7EC), Color(0xFF1F2F98))
                            ),
                            shape = RoundedCornerShape(25.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Đăng kí",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Đã có tài khoản?", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đăng nhập",
                        color = Color(0xFF787FF6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { onSignInClick() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview(){
    RegisterContent(
        username = "user",
        email = "user@gmail.com",
        password = "abc",

        confirmPassword = "abc",

        isLoading = false,
        errorMessage = null,

        onUsernameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onRegisterClick = { _, _, _, _, _ -> },
        onSignInClick = {}
    )
}