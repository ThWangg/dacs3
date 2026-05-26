package ltdd.dacsba.groceries.data.repository

import ltdd.dacsba.groceries.data.model.Product
import kotlin.math.sqrt

/**
 * 3.4 Cơ sở thuật toán của hệ thống khuyến nghị
 * 3.4.2 Công thức toán học và quy trình tính toán điểm
 */
object ContentBasedFilteringEngine {

    /**
     * Bước 1: Định dạng Vector đặc trưng và Bước 2: Tính độ tương đồng bằng công thức Cosine
     *
     * - U: Vector sở thích của người dùng (tất cả phần tử đều bằng 1 cho các nhãn đã mua)
     * - P: Vector sản phẩm cần kiểm tra (1 nếu sản phẩm chứa nhãn sở thích, 0 nếu ngược lại)
     *
     * Sim(U, P) = (U . P) / (|U| * |P|)
     *
     * @param userTags Tập hợp các nhãn sở thích của người dùng
     * @param productTags Danh sách các nhãn của sản phẩm cần tính độ tương đồng
     * @return Độ tương đồng nằm trong khoảng [0, 1]
     */
    fun calculateSimilarity(userTags: Set<String>, productTags: List<String>): Double {
        if (userTags.isEmpty() || productTags.isEmpty()) return 0.0

        val uList = userTags.toList()
        val n = uList.size

        // Bước 1: Định dạng Vector đặc trưng
        val vectorU = DoubleArray(n) { 1.0 }
        val vectorP = DoubleArray(n) { 0.0 }

        for (i in 0 until n) {
            val tag = uList[i]
            if (productTags.contains(tag)) {
                vectorP[i] = 1.0
            }
        }

        // Bước 2: Tính độ tương đồng bằng công thức Cosine
        // Tích vô hướng: U . P
        var dotProduct = 0.0
        for (i in 0 until n) {
            dotProduct += vectorU[i] * vectorP[i]
        }

        // Độ dài vector U: |U|
        var sumUSq = 0.0
        for (i in 0 until n) {
            sumUSq += vectorU[i] * vectorU[i]
        }
        val magU = sqrt(sumUSq)

        // Độ dài vector P: |P|
        var sumPSq = 0.0
        for (i in 0 until n) {
            sumPSq += vectorP[i] * vectorP[i]
        }
        val magP = sqrt(sumPSq)

        if (magU == 0.0 || magP == 0.0) return 0.0

        return dotProduct / (magU * magP)
    }

    /**
     * Xếp hạng danh sách sản phẩm theo độ tương đồng giảm dần.
     *
     * @param userTags Tập hợp nhãn sở thích của người dùng
     * @param candidateProducts Danh sách sản phẩm để xếp hạng gợi ý
     * @return Danh sách sản phẩm được sắp xếp theo độ tương đồng giảm dần (> 0.0)
     */
    fun rankProducts(
        userTags: Set<String>,
        candidateProducts: List<Product>
    ): List<Pair<Product, Double>> {
        if (userTags.isEmpty()) return emptyList()

        return candidateProducts
            .map { product ->
                val similarity = calculateSimilarity(userTags, product.tags)
                product to similarity
            }
            .filter { (_, sim) -> sim > 0.0 }
            .sortedByDescending { (_, sim) -> sim }
    }
}
