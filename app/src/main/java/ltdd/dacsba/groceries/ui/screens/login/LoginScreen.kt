package ltdd.dacsba.groceries.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ltdd.dacsba.groceries.R
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.ui.components.AppTextField

@Composable
fun LoginScreen( //logic
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

    LoginContent(
        email = email,
        password = password,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onLoginClick = { authViewModel.login(email, password) },
        onSignUpClick = { navController.navigate(AppConstant.Routes.REGISTER) }
    )
}

@Composable
fun LoginContent( // UI để preview
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {

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
                .padding(horizontal = 40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            //logo
            Row(
                modifier = Modifier.padding(top = 180.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(80.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.tuat_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(height = 70.dp, width = 70.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.width(60.dp),
                        thickness = 4.dp,
                        color = Color(0xFFBB86FC)
                    )
                    Text(
                        text = "TAUT Shop",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE26161)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Please sign in to continue",
                color = Color.Gray,
                fontSize = 16.sp)

            //email
            AppTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                modifier = Modifier.padding(top = 20.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            //pass
            AppTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                isPassword = true,
                modifier = Modifier.padding(top = 15.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            //if( lỗi)
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            //login
            Button(
                onClick = onLoginClick,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7CB342)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(180.dp)
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Have no account?", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign up",
                    color = Color(0xFF7CB342),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginContent(
        email = "user@gmail.com",
        password = "abc",
        isLoading = false,
        errorMessage = null,
        onEmailChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onSignUpClick = {}
    )
}