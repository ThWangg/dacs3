package ltdd.dacsba.groceries.ui.screens.user

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ltdd.dacsba.groceries.ui.components.AppBottomBar
import ltdd.dacsba.groceries.ui.components.BottomNavItem
import ltdd.dacsba.groceries.ui.screens.user.AccentOrange

object BuyerRoutes {
    const val HOME = "buyer_home_tab"
    const val CART = "buyer_cart"
    const val ORDERS = "buyer_orders"
    const val PROFILE = "buyer_profile"
    const val CHECKOUT = "buyer_checkout"
}

@Composable
fun MainBuyerScreen(
    parentNavController: NavController,
    onLogout: () -> Unit,
    onSwitchToSeller: () -> Unit
) {
    val navController = rememberNavController()
    val buyerViewModel: BuyerHomeViewModel = viewModel()
    val cartViewModel: BuyerCartViewModel = viewModel()

    val bottomNavItems = listOf(
        BottomNavItem("Trang chủ", BuyerRoutes.HOME, Icons.Default.Home),
        BottomNavItem("Giỏ hàng", BuyerRoutes.CART, Icons.Default.ShoppingCart),
        BottomNavItem("Đơn hàng", BuyerRoutes.ORDERS, Icons.Default.List),
        BottomNavItem("Cá nhân", BuyerRoutes.PROFILE, Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController,
                items = bottomNavItems,
                selectedColor = AccentOrange
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BuyerRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BuyerRoutes.HOME) {
                BuyerHomeScreen(viewModel = buyerViewModel, navController = navController)
            }
            composable(BuyerRoutes.CART) {
                BuyerCartScreen(navController = navController, viewModel = cartViewModel)
            }
            composable(BuyerRoutes.CHECKOUT) {
                BuyerCheckoutScreen(navController = navController, viewModel = cartViewModel)
            }
            composable(BuyerRoutes.ORDERS) {
                BuyerOrderScreen(navController = navController)
            }
            composable(BuyerRoutes.PROFILE) {
                BuyerProfileScreen(
                    navController = navController,
                    viewModel = buyerViewModel,
                    onLogout = onLogout,
                    onSwitchToSeller = onSwitchToSeller
                )
            }
        }
    }
}
