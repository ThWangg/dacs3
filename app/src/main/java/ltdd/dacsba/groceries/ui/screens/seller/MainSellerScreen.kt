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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ltdd.dacsba.groceries.data.constant.AppConstant
import ltdd.dacsba.groceries.ui.components.AppBottomBar
import ltdd.dacsba.groceries.ui.components.BottomNavItem

object SellerRoutes {
    const val DASHBOARD = "seller_dashboard"
    const val PRODUCTS = "seller_products"
    const val ORDERS = "seller_orders"
    const val PROFILE = "seller_profile"
    const val ADD_PRODUCT = "seller_add_product"
    const val EDIT_PRODUCT = "seller_edit_product"
    const val NOTIFICATIONS = "seller_notifications"
}

@Composable
fun MainSellerScreen(
    onLogout: (String?) -> Unit,
    onSwitchToBuyer: () -> Unit
) {
    val navController = rememberNavController()
    val sellerViewModel: SellerViewModel = viewModel()

    val auth = androidx.compose.runtime.remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val db = androidx.compose.runtime.remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }

    androidx.compose.runtime.DisposableEffect(auth.currentUser) {
        val uid = auth.currentUser?.uid ?: return@DisposableEffect onDispose {}
        val listener = db.collection(ltdd.dacsba.groceries.data.constant.AppConstant.COLLECTION_USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val isDeactivated = snapshot.getBoolean("isDeactivated") ?: false
                    if (isDeactivated) {
                        auth.signOut()
                        onLogout("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.")
                    }
                }
            }

        onDispose {
            listener.remove()
        }
    }

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
                selectedColor = SellerGreen
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
                SellerProfileScreen(
                    navController = navController,
                    onLogout = { onLogout(null) },
                    onSwitchToBuyer = onSwitchToBuyer
                )
            }
            composable(AppConstant.Routes.SELLER_ADD_PRODUCT) {
                SellerAddProductScreen(navController = navController, viewModel = sellerViewModel)
            }
            composable("${AppConstant.Routes.SELLER_EDIT_PRODUCT}/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                SellerEditProductScreen(navController = navController, productId = productId)
            }
            composable(SellerRoutes.NOTIFICATIONS) {
                SellerNotificationScreen(navController = navController, viewModel = sellerViewModel)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainSellerScreenPreview() {
    MainSellerScreen(onLogout = {}, onSwitchToBuyer = {})
}
