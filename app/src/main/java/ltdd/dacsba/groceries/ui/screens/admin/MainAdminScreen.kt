package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ltdd.dacsba.groceries.ui.components.AppBottomBar
import ltdd.dacsba.groceries.ui.components.BottomNavItem

// Routes nội bộ của Admin
object AdminRoutes {
    const val USERS = "admin_users"
    const val SHOP_APPROVAL = "admin_shop_approval"
    const val CATEGORIES = "admin_categories"
}

// Màu accent cho admin tab bar
private val AdminPrimary = Color(0xFF787FF6)

@Composable
fun MainAdminScreen() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Người dùng", AdminRoutes.USERS, Icons.Default.Person),
        BottomNavItem("Duyệt shop", AdminRoutes.SHOP_APPROVAL, Icons.Default.Store),
        BottomNavItem("Danh mục", AdminRoutes.CATEGORIES, Icons.Default.Category)
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController,
                items = bottomNavItems,
                selectedColor = AdminPrimary
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AdminRoutes.USERS,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminRoutes.USERS) {
                AdminUserScreen()
            }
            composable(AdminRoutes.SHOP_APPROVAL) {
                AdminShopApprovalScreen()
            }
            composable(AdminRoutes.CATEGORIES) {
                AdminCategoryScreen()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAdminScreenPreview() {
    MainAdminScreen()
}
