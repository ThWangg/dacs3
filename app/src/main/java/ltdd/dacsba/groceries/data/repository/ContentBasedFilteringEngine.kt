package ltdd.dacsba.groceries.data.repository

import ltdd.dacsba.groceries.data.model.Product
import kotlin.math.sqrt

object ContentBasedFilteringEngine {

fun calculateSimilarity(userTags: Set<String>, productTags: List<String>): Double {
        if (userTags.isEmpty() || productTags.isEmpty()) return 0.0

        val uList = userTags.toList()
        val n = uList.size

val vectorU = DoubleArray(n) { 1.0 }
        val vectorP = DoubleArray(n) { 0.0 }

        for (i in 0 until n) {
            val tag = uList[i]
            if (productTags.contains(tag)) {
                vectorP[i] = 1.0
            }
        }

var dotProduct = 0.0
        for (i in 0 until n) {
            dotProduct += vectorU[i] * vectorP[i]
        }

var sumUSq = 0.0
        for (i in 0 until n) {
            sumUSq += vectorU[i] * vectorU[i]
        }
        val magU = sqrt(sumUSq)

var sumPSq = 0.0
        for (i in 0 until n) {
            sumPSq += vectorP[i] * vectorP[i]
        }
        val magP = sqrt(sumPSq)

        if (magU == 0.0 || magP == 0.0) return 0.0

        return dotProduct / (magU * magP)
    }

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
