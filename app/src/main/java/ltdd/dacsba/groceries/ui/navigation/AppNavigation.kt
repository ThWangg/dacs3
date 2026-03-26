package ltdd.dacsba.groceries.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.ui.screens.login.LoginScreen
import ltdd.dacsba.groceries.ui.screens.login.RegisterScreen
import ltdd.dacsba.groceries.ui.screens.seller.MainSellerScreen
import ltdd.dacsba.groceries.ui.screens.seller.SellerAddProductScreen
import ltdd.dacsba.groceries.ui.screens.seller.SellerEditProductScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppConstant.Routes.LOGIN
    ) {

        // Auth routes
        composable(AppConstant.Routes.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(AppConstant.Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        // Seller main screen (có bottom bar tích hợp sẵn)
        composable(AppConstant.Routes.SELLER_HOME) {
            MainSellerScreen()
        }

        // Seller sub-screens (navigate từ SellerProductScreen)
        composable(AppConstant.Routes.SELLER_ADD_PRODUCT) {
            SellerAddProductScreen(navController = navController)
        }
        composable("${AppConstant.Routes.SELLER_EDIT_PRODUCT}/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            SellerEditProductScreen(navController = navController, productId = productId)
        }
    }
}