package ltdd.dacsba.groceries.data.model

data class Category(
    val categoryId: String = "",
    val categoryName: String = "",
    val iconEmoji: String = "",
    val availableUnits: List<String> = emptyList()
) {
    companion object {
        // Danh sách category cố định, có thể mở rộng hoặc load từ Firestore
        val defaultCategories = listOf(
            Category(
                categoryId = "fresh_fruit",
                categoryName = "Trái cây tươi",
                iconEmoji = "🍎",
                availableUnits = listOf("kg", "g", "hộp", "túi", "quả")
            ),
            Category(
                categoryId = "fresh_veggie",
                categoryName = "Rau củ tươi",
                iconEmoji = "🥦",
                availableUnits = listOf("kg", "g", "bó", "túi", "mớ")
            ),
            Category(
                categoryId = "meat_seafood",
                categoryName = "Thịt & Hải sản",
                iconEmoji = "🥩",
                availableUnits = listOf("kg", "g", "miếng", "con", "hộp")
            ),
            Category(
                categoryId = "dairy_egg",
                categoryName = "Sữa & Trứng",
                iconEmoji = "🥛",
                availableUnits = listOf("hộp", "chai", "lốc", "vỉ", "cái")
            ),
            Category(
                categoryId = "bakery",
                categoryName = "Bánh & Ngũ cốc",
                iconEmoji = "🍞",
                availableUnits = listOf("ổ", "hộp", "túi", "gói", "kg")
            ),
            Category(
                categoryId = "beverage",
                categoryName = "Đồ uống",
                iconEmoji = "🧃",
                availableUnits = listOf("chai", "lon", "lốc", "thùng", "hộp")
            ),
            Category(
                categoryId = "condiment",
                categoryName = "Gia vị & Nước chấm",
                iconEmoji = "🧂",
                availableUnits = listOf("chai", "hộp", "gói", "kg", "g")
            ),
            Category(
                categoryId = "frozen",
                categoryName = "Thực phẩm đông lạnh",
                iconEmoji = "🧊",
                availableUnits = listOf("kg", "g", "hộp", "túi", "gói")
            ),
            Category(
                categoryId = "snack",
                categoryName = "Đồ ăn vặt",
                iconEmoji = "🍿",
                availableUnits = listOf("gói", "hộp", "túi", "cái", "lốc")
            ),
            Category(
                categoryId = "organic",
                categoryName = "Hữu cơ & Sạch",
                iconEmoji = "🌿",
                availableUnits = listOf("kg", "g", "túi", "bó", "hộp")
            )
        )

        // Tất cả các đơn vị phổ biến (dùng khi chưa chọn category)
        val allUnits = listOf("kg", "g", "lít", "ml", "hộp", "chai", "gói", "túi", "lon", "lốc", "bó", "quả", "cái", "con", "miếng", "thùng")
    }
}
