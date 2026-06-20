package ltdd.dacsba.groceries.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RatingAccent = Color(0xFF787FF6)

/**
 * Thanh chọn điểm từ 1.0 đến 5.0, bước 0.5.
 * Không dùng sao — chỉ hiển thị các ô số.
 */
@Composable
fun RatingBar(
    selected: Double,
    onSelect: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            steps.forEach { value ->
                val isSelected = selected == value
                val label = if (value == value.toLong().toDouble()) {
                    value.toInt().toString()
                } else {
                    value.toString()
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) RatingAccent else RatingAccent.copy(alpha = 0.08f)
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) RatingAccent else RatingAccent.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else RatingAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Label hiển thị điểm đang chọn và ý nghĩa
        if (selected > 0.0) {
            val desc = when {
                selected <= 1.5 -> "Rất tệ"
                selected <= 2.5 -> "Tạm được"
                selected <= 3.5 -> "Bình thường"
                selected <= 4.5 -> "Tốt"
                else            -> "Xuất sắc"
            }
            Text(
                text = "Điểm: $selected / 5.0  —  $desc",
                fontSize = 12.sp,
                color = RatingAccent,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "Chọn điểm đánh giá (1.0 — 5.0)",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Hiển thị điểm rating nhỏ gọn: "4.5 (12)"
 */
@Composable
fun RatingDisplay(
    ratingAverage: Double,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    if (reviewCount <= 0) return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "★",
            fontSize = 12.sp,
            color = Color(0xFFFFC107)
        )
        Text(
            text = String.format("%.1f", ratingAverage),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )
        Text(
            text = "($reviewCount)",
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}
