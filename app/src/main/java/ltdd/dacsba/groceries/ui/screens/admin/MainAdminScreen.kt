package ltdd.dacsba.groceries.ui.screens.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ltdd.dacsba.groceries.ui.components.AppBottomBar
import ltdd.dacsba.groceries.ui.components.BottomNavItem

object AdminRoutes {
    const val DASHBOARD = "admin_dashboard"
    const val USERS     = "admin_users"
    const val PRODUCTS  = "admin_products"
    const val REQUESTS  = "admin_requests"
    const val PROFILE   = "admin_profile"
}

@Composable
fun MainAdminScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val adminViewModel: AdminViewModel = viewModel()
    val snack by adminViewModel.snackMessage
    val pendingCount by remember { derivedStateOf { adminViewModel.pendingRequests.value.size } }
    val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(Unit) { adminViewModel.loadPendingRequests() }

    LaunchedEffect(snack) {
        snack?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearSnack()
        }
    }

    val bottomItems = listOf(
        BottomNavItem("Dashboard",  AdminRoutes.DASHBOARD, Icons.Default.Dashboard),
        BottomNavItem("Users",      AdminRoutes.USERS,     Icons.Default.People),
        BottomNavItem("Sản phẩm",   AdminRoutes.PRODUCTS,  Icons.Default.Inventory),
        BottomNavItem("Profile",    AdminRoutes.PROFILE,   Icons.Default.ManageAccounts),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomBar(
                navController = navController,
                items = bottomItems,
                selectedColor = AdminGreen
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AdminRoutes.DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminRoutes.DASHBOARD) {
                AdminDashboardScreen(navController = navController, viewModel = adminViewModel)
            }
            composable(AdminRoutes.USERS) {
                AdminUsersScreen(navController = navController, viewModel = adminViewModel)
            }
            composable(AdminRoutes.PRODUCTS) {
                AdminProductsScreen(navController = navController, viewModel = adminViewModel)
            }
            composable(AdminRoutes.REQUESTS) {
                AdminRequestsScreen(navController = navController, viewModel = adminViewModel)
            }
            composable(AdminRoutes.PROFILE) {
                AdminProfileScreen(
                    navController = navController,
                    viewModel = adminViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAdminScreenPreview() {
    MainAdminScreen(onLogout = {})
}
