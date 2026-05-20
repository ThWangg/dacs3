package ltdd.dacsba.groceries.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.ui.screens.admin.MainAdminScreen
import ltdd.dacsba.groceries.ui.screens.login.LoginScreen
import ltdd.dacsba.groceries.ui.screens.login.RegisterScreen
import ltdd.dacsba.groceries.ui.screens.seller.MainSellerScreen
import ltdd.dacsba.groceries.ui.screens.seller.SellerAddProductScreen
import ltdd.dacsba.groceries.ui.screens.seller.SellerEditProductScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val onLogoutAction: (String?) -> Unit = { msg ->
        val route = if (msg != null) {
            "${AppConstant.Routes.LOGIN}?message=$msg"
        } else {
            AppConstant.Routes.LOGIN
        }
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppConstant.Routes.LOGIN
    ) {

        // Auth routes
        composable(
            route = "${AppConstant.Routes.LOGIN}?message={message}",
            arguments = listOf(
                androidx.navigation.navArgument("message") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message")
            LoginScreen(navController = navController, initialMessage = message)
        }
        composable(AppConstant.Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        // Admin home screen
        composable(AppConstant.Routes.ADMIN_HOME) {
            MainAdminScreen(
                onLogout = { onLogoutAction(null) }
            )
        }

        // Buyer home screen
        composable(AppConstant.Routes.BUYER_HOME) {
            ltdd.dacsba.groceries.ui.screens.user.MainBuyerScreen(
                parentNavController = navController,
                onLogout = onLogoutAction,
                onSwitchToSeller = {
                    navController.navigate(AppConstant.Routes.SELLER_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Seller main screen (có bottom bar tích hợp sẵn)
        composable(AppConstant.Routes.SELLER_HOME) {
            MainSellerScreen(
                onLogout = onLogoutAction,
                onSwitchToBuyer = {
                    navController.navigate(AppConstant.Routes.BUYER_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}