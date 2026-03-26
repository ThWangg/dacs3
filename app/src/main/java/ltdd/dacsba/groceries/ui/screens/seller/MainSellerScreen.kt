package ltdd.dacsba.groceries.ui.screens.seller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ltdd.dacsba.groceries.ui.components.AppBottomBar
import ltdd.dacsba.groceries.ui.components.BottomNavItem

object SellerRoutes {
    const val DASHBOARD = "seller_dashboard"
    const val PRODUCTS = "seller_products"
    const val ORDERS = "seller_orders"
    const val PROFILE = "seller_profile"
}

@Composable
fun MainSellerScreen() {
    val navController = rememberNavController()
    val sellerViewModel: SellerViewModel = viewModel()

    val bottomNavItems = listOf(
        BottomNavItem("Dashboard", SellerRoutes.DASHBOARD, Icons.Default.Home),
        BottomNavItem("Products", SellerRoutes.PRODUCTS, Icons.Default.ShoppingCart),
        BottomNavItem("Orders", SellerRoutes.ORDERS, Icons.Default.List),
        BottomNavItem("Profile", SellerRoutes.PROFILE, Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController,
                items = bottomNavItems,
                selectedColor = Color(0xFF7CB342)
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = SellerRoutes.DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(SellerRoutes.DASHBOARD) {
                SellerDashboardScreen(
                    navController = navController,
                    viewModel = sellerViewModel
                )
            }
            composable(SellerRoutes.PRODUCTS) {
                SellerProductScreen(
                    navController = navController,
                    viewModel = sellerViewModel
                )
            }
            composable(SellerRoutes.ORDERS) {
                SellerOrderScreen(
                    navController = navController,
                    viewModel = sellerViewModel
                )
            }
            composable(SellerRoutes.PROFILE) {
                SellerProfileScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainSellerScreenPreview() {
    MainSellerScreen()
}
