package ltdd.dacsba.groceries.data.model

import java.net.URLEncoder

/**
 * Cấu hình tài khoản nhận tiền + bộ sinh URL QR động từ VietQR.io API quốc gia.
 * Không cần thư viện sinh QR phía client – chỉ load ảnh từ URL qua Coil.
 *
 * URL format chuẩn:
 * https://img.vietqr.io/image/{bankId}-{accountNo}-{template}.png
 *   ?amount={số_tiền_VND}
 *   &addInfo={nội_dung_chuyển_khoản}
 *   &accountName={tên_tài_khoản}
 */
data class PaymentQrConfig(
    val bankId: String = "MB",                      // Bank ID chuẩn VietQR (MBBank = MB / BIN 970422)
    val accountNo: String = "0123456789",           // Số tài khoản demo
    val accountName: String = "TAUT SHOP DEMO",     // Tên chủ tài khoản demo
    val template: String = "compact2"               // compact | compact2 | qr_only | print
) {
    /**
     * Sinh URL ảnh QR động từ API VietQR.io
     * @param amount Số tiền VND (Long)
     * @param addInfo Nội dung chuyển khoản, vd: "DONHANG ORD123"
     */
    fun buildQrImageUrl(amount: Long, addInfo: String): String {
        val encodedInfo = URLEncoder.encode(addInfo, "UTF-8")
        val encodedName = URLEncoder.encode(accountName, "UTF-8")
        return "https://img.vietqr.io/image/$bankId-$accountNo-$template.png" +
                "?amount=$amount" +
                "&addInfo=$encodedInfo" +
                "&accountName=$encodedName"
    }

    companion object {
        /** Instance mặc định dùng cho toàn app */
        val DEFAULT = PaymentQrConfig()
    }
}
