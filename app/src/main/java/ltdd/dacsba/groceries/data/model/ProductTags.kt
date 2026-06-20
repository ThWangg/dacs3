package ltdd.dacsba.groceries.data.model

object ProductTags {

const val RAU_CU        = "rau_cu"
    const val TRAI_CAY      = "trai_cay"
    const val THIT          = "thit"
    const val HAI_SAN       = "hai_san"
    const val TRUNG         = "trung"
    const val SUA           = "sua"
    const val BANH          = "banh"
    const val NGU_COC       = "ngu_coc"
    const val DO_UONG       = "do_uong"
    const val GIA_VI        = "gia_vi"
    const val DO_AN_VAT     = "do_an_vat"

const val HUU_CO        = "huu_co"
    const val SACH          = "sach"
    const val NHAP_KHAU     = "nhap_khau"
    const val TRONG_NUOC    = "trong_nuoc"
    const val CHUNG_NHAN    = "chung_nhan_vietgap"

const val DO_TUOI       = "do_tuoi"
    const val DONG_LANH     = "dong_lanh"
    const val KHO           = "kho"
    const val TUOI_SONG     = "tuoi_song"
    const val DA_SO_CHE     = "da_so_che"
    const val PHI_LE        = "phi_le"
    const val CO_XUONG      = "co_xuong"

const val GIAM_CAN      = "giam_can"
    const val NHIEU_PROTEIN = "nhieu_protein"
    const val IT_CALO       = "it_calo"
    const val KHONG_GLUTEN  = "khong_gluten"
    const val AN_CHAY       = "an_chay"
    const val SANG_LOC      = "sang_loc"

const val DONG_GOI_SAN  = "dong_goi_san"
    const val BAN_LE        = "ban_le"
    const val SUA_HOT       = "sua_hot"

val suggestedTagsByCategory: Map<String, List<String>> = mapOf(
        "fresh_veggie" to listOf(
            RAU_CU, HUU_CO, DO_TUOI, SACH, TRONG_NUOC, NHAP_KHAU,
            CHUNG_NHAN, GIAM_CAN, IT_CALO, AN_CHAY, DONG_GOI_SAN
        ),
        "fresh_fruit" to listOf(
            TRAI_CAY, DO_TUOI, HUU_CO, NHAP_KHAU, TRONG_NUOC, SANG_LOC,
            SACH, GIAM_CAN, IT_CALO, AN_CHAY, DONG_GOI_SAN
        ),
        "meat_seafood" to listOf(
            THIT, HAI_SAN, TUOI_SONG, DONG_LANH, SACH, NHAP_KHAU,
            TRONG_NUOC, CO_XUONG, PHI_LE, DA_SO_CHE, NHIEU_PROTEIN
        ),
        "dairy_egg" to listOf(
            SUA, TRUNG, NHAP_KHAU, TRONG_NUOC, SACH, HUU_CO,
            NHIEU_PROTEIN, DONG_GOI_SAN, SUA_HOT
        ),
        "bakery" to listOf(
            BANH, NGU_COC, DONG_GOI_SAN, KHONG_GLUTEN, AN_CHAY,
            NHAP_KHAU, TRONG_NUOC, DO_AN_VAT
        ),
        "beverage" to listOf(
            DO_UONG, HUU_CO, NHAP_KHAU, TRONG_NUOC, IT_CALO,
            GIAM_CAN, AN_CHAY, DONG_GOI_SAN
        ),
        "condiment" to listOf(
            GIA_VI, NHAP_KHAU, TRONG_NUOC, SACH, HUU_CO,
            DONG_GOI_SAN, KHO
        ),
        "frozen" to listOf(
            DONG_LANH, THIT, HAI_SAN, DA_SO_CHE, NHAP_KHAU,
            TRONG_NUOC, DONG_GOI_SAN
        ),
        "snack" to listOf(
            DO_AN_VAT, DONG_GOI_SAN, NHAP_KHAU, TRONG_NUOC,
            KHONG_GLUTEN, AN_CHAY, IT_CALO
        ),
        "organic" to listOf(
            HUU_CO, SACH, CHUNG_NHAN, RAU_CU, TRAI_CAY,
            GIAM_CAN, IT_CALO, AN_CHAY, TRONG_NUOC
        )
    )

data class TagGroup(val groupName: String, val tags: List<String>)

    val displayGroups: List<TagGroup> = listOf(
        TagGroup(
            "🥗 Loại thực phẩm",
            listOf(RAU_CU, TRAI_CAY, THIT, HAI_SAN, TRUNG, SUA, BANH, NGU_COC, DO_UONG, GIA_VI, DO_AN_VAT)
        ),
        TagGroup(
            "🌱 Chất lượng & Xuất xứ",
            listOf(HUU_CO, SACH, NHAP_KHAU, TRONG_NUOC, CHUNG_NHAN)
        ),
        TagGroup(
            "❄️ Trạng thái & Chế biến",
            listOf(DO_TUOI, DONG_LANH, KHO, TUOI_SONG, DA_SO_CHE, PHI_LE, CO_XUONG)
        ),
        TagGroup(
            "💪 Dinh dưỡng",
            listOf(GIAM_CAN, NHIEU_PROTEIN, IT_CALO, KHONG_GLUTEN, AN_CHAY, SANG_LOC)
        )
    )

val tagDisplayName: Map<String, String> = mapOf(
        RAU_CU        to "Rau củ",
        TRAI_CAY      to "Trái cây",
        THIT          to "Thịt",
        HAI_SAN       to "Hải sản",
        TRUNG         to "Trứng",
        SUA           to "Sữa",
        BANH          to "Bánh",
        NGU_COC       to "Ngũ cốc",
        DO_UONG       to "Đồ uống",
        GIA_VI        to "Gia vị",
        DO_AN_VAT     to "Đồ ăn vặt",
        HUU_CO        to "Hữu cơ",
        SACH          to "Sạch",
        NHAP_KHAU     to "Nhập khẩu",
        TRONG_NUOC    to "Trong nước",
        CHUNG_NHAN    to "VietGAP",
        DO_TUOI       to "Đồ tươi",
        DONG_LANH     to "Đông lạnh",
        KHO           to "Khô",
        TUOI_SONG     to "Tươi sống",
        DA_SO_CHE     to "Đã sơ chế",
        PHI_LE        to "Phi lê",
        CO_XUONG      to "Có xương",
        GIAM_CAN      to "Giảm cân",
        NHIEU_PROTEIN to "Nhiều protein",
        IT_CALO       to "Ít calo",
        KHONG_GLUTEN  to "Không gluten",
        AN_CHAY       to "Ăn chay",
        SANG_LOC      to "Sàng lọc",
        DONG_GOI_SAN  to "Đóng gói sẵn",
        BAN_LE        to "Bán lẻ",
        SUA_HOT       to "Sữa hạt"
    )

fun displayNameOf(tag: String): String = tagDisplayName[tag] ?: tag.replace("_", " ")

    const val MAX_TAGS = 10

val autoTagsByCategory: Map<String, List<String>> = mapOf(
        "fresh_veggie" to listOf(RAU_CU, "thuc_pham_xanh"),
        "fresh_fruit" to listOf(TRAI_CAY, "thuc_pham_xanh"),
        "meat_seafood" to listOf("thuc_pham_song"),
        "dairy_egg" to listOf(SUA, TRUNG),
        "organic" to listOf(HUU_CO, SACH)
    )

    val keywordToTag: Map<String, String> = mapOf(
        "sạch" to SACH,
        "đà lạt" to "da_lat",
        "combo" to "combo",
        "hữu cơ" to HUU_CO,
        "tươi" to DO_TUOI,
        "nhập khẩu" to NHAP_KHAU,
        "nguyên chất" to "nguyen_chat",
        "giảm cân" to GIAM_CAN,
        "diet" to GIAM_CAN,
        "chay" to AN_CHAY,
        "vietgap" to CHUNG_NHAN,
        "việt gap" to CHUNG_NHAN
    )

fun generateAutoTags(categoryId: String, productName: String): List<String> {
        val tags = mutableSetOf<String>()

autoTagsByCategory[categoryId]?.let { tags.addAll(it) }

val lowerName = productName.lowercase()
        for ((keyword, tag) in keywordToTag) {
            if (lowerName.contains(keyword)) {
                tags.add(tag)
            }
        }
        
        return tags.toList()
    }
}

fun Product.itemProfile(): List<String> = tags
