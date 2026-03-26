package ltdd.dacsba.groceries.data.constant

object AppConstant {

    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_CATEGORIES = "categories"

    object Roles {
        const val ADMIN = "ADMIN"
        const val SELLER = "SELLER"
        const val BUYER = "BUYER"
    }

    object Routes {
        const val LOGIN = "login_screen"
        const val REGISTER = "register_screen"
        const val BUYER_HOME = "buyer_home_screen"
        const val ADMIN_HOME = "admin_home_screen"
        const val SELLER_HOME = "seller_home_screen"
        const val SELLER_ADD_PRODUCT = "seller_add_product"
        const val SELLER_EDIT_PRODUCT = "seller_edit_product"
    }

}