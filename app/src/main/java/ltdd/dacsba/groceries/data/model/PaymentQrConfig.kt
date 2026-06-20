package ltdd.dacsba.groceries.data.model

import java.net.URLEncoder

data class PaymentQrConfig(
    val bankId: String = "MB",
    val accountNo: String = "0123456789",
    val accountName: String = "TAUT SHOP DEMO",
    val template: String = "compact2"
) {
    
    fun buildQrImageUrl(amount: Long, addInfo: String): String {
        val encodedInfo = URLEncoder.encode(addInfo, "UTF-8")
        val encodedName = URLEncoder.encode(accountName, "UTF-8")
        return "https://img.vietqr.io/image/$bankId-$accountNo-$template.png" +
                "?amount=$amount" +
                "&addInfo=$encodedInfo" +
                "&accountName=$encodedName"
    }

    companion object {
        
        val DEFAULT = PaymentQrConfig()
    }
}
